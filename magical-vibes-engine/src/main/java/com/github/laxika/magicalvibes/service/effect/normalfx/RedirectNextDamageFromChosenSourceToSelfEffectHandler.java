package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceKeyedRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link RedirectNextDamageFromChosenSourceToSelfEffect}: one {@link SourceKeyedRedirectShield} until end of
 * turn for each source the effect names, with the recipients and the destination fixed as it resolves. A spell source
 * keeps its card's graveyard stay, so a later cast of that card is not covered (CR 400.7). With its destination
 * permanent gone, or no source, nothing happens.
 */
@Component
@RequiredArgsConstructor
public class RedirectNextDamageFromChosenSourceToSelfEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final DamageSourceChoiceSupport damageSourceChoiceSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageFromChosenSourceToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RedirectNextDamageFromChosenSourceToSelfEffect) effect;
        UUID destinationId = null;
        String destinationName = null;
        if (e.toPermanent() != null) {
            Permanent destination = subjectResolutionService.permanent(gameData, entry, effect, e.toPermanent());
            if (destination == null) {
                return;
            }
            destinationId = destination.getId();
            destinationName = FaceDownVisibility.publicName(gameData, destination);
        } else if (e.toPlayer() != null) {
            List<UUID> players = subjectResolutionService.players(gameData, entry, effect, e.toPlayer());
            if (players.isEmpty()) {
                return;
            }
            destinationId = players.getFirst();
            destinationName = gameData.playerIdToName.get(destinationId);
        }
        Set<UUID> protectedPlayers = e.recipients() == null ? null
                : Set.copyOf(new LinkedHashSet<>(subjectResolutionService.players(gameData, entry, effect,
                        e.recipients())));
        for (UUID sourceId : subjectResolutionService.damageSourceIds(gameData, entry, effect, e.source())) {
            boolean spell = gameQueryService.findPermanentById(gameData, sourceId) == null
                    && gameData.stack.stream().anyMatch(onStack -> onStack.isIdentifiedBy(sourceId));
            gameData.addShield(new SourceKeyedRedirectShield(sourceId,
                    spell ? gameData.graveyardEntryVersion(sourceId) : -1, protectedPlayers, destinationId,
                    e.wholeTurn(), e.combatOnly()));
            Card sourceCard = damageSourceChoiceSupport.sourceCard(gameData, sourceId);
            if (sourceCard == null) {
                continue;
            }
            gameData.addWaitingDamageSource(sourceId, sourceCard);
            Permanent permanent = gameQueryService.findPermanentById(gameData, sourceId);
            String sourceName = permanent != null ? FaceDownVisibility.publicName(gameData, permanent)
                    : FaceDownVisibility.publicName(gameData, sourceCard);
            gameLogService.append(gameData, GameLog.text(logLine(gameData, e, sourceName, protectedPlayers,
                    destinationName)));
        }
    }

    private static String logLine(GameData gameData, RedirectNextDamageFromChosenSourceToSelfEffect e,
                                  String sourceName, Set<UUID> protectedPlayers, String destinationName) {
        String damage = e.combatOnly() ? "combat damage" : "damage";
        String recipients = protectedPlayers == null ? ""
                : " to " + String.join(" or ", protectedPlayers.stream().map(gameData.playerIdToName::get).toList());
        String destination = destinationName != null ? destinationName : sourceName + "'s controller";
        return e.wholeTurn()
                ? "All " + damage + " " + sourceName + " would deal" + recipients + " this turn is dealt to "
                        + destination + " instead."
                : "The next time " + sourceName + " would deal " + damage + recipients + " this turn, that damage is"
                        + " dealt to " + destination + " instead.";
    }
}
