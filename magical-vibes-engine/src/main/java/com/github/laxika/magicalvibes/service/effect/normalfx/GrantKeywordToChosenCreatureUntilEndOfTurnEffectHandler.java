package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantKeywordToChosenCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToChosenCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantKeywordToChosenCreatureUntilEndOfTurnEffect) effect;
        UUID chosenCreatureId = e.chosenCreatureId() != null ? e.chosenCreatureId() : entry.getChosenPermanentId();
        if (chosenCreatureId == null) {
            if (entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of());
            List<UUID> eligibleIds = new ArrayList<>();
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    eligibleIds.add(permanent.getId());
                }
            }
            if (eligibleIds.isEmpty()) {
                return;
            }
            if (eligibleIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ChooseOwnCreatureGrantKeyword(e.keyword()));
                playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), eligibleIds,
                        "Choose a creature you control.");
                return;
            }
            chosenCreatureId = eligibleIds.getFirst();
        }

        Permanent target = gameQueryService.findPermanentById(gameData, chosenCreatureId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            log.info("Game {} - Chosen creature no longer on battlefield", gameData.id);
            return;
        }

        applyEffect(gameData, entry, target, e.keyword());
        gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                .text(" gains " + formatKeyword(e.keyword()) + " until end of turn.").build());
        log.info("Game {} - {} gains {} (chosen creature)", gameData.id, target.getCard().getName(), e.keyword());
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.ChooseOwnCreatureGrantKeyword context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect is waiting for a creature choice");
        }
        Permanent target = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (target == null
                || !gameQueryService.isCreature(gameData, target)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, chosenPermanentId))) {
            throw new IllegalStateException("Choose a creature you control");
        }
        applyEffect(gameData, entry, target, context.keyword());
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " gains " + formatKeyword(context.keyword()) + " until end of turn."));
    }

    private void applyEffect(GameData gameData, StackEntry entry, Permanent target, Keyword keyword) {
        if (gameQueryService.cantHaveOrGainKeyword(gameData, target, keyword)) {
            return;
        }
        target.getGrantedKeywords().add(keyword);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(),
                new GrantKeywordEffect(Set.of(keyword), GrantScope.TARGET),
                target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
    }

    private String formatKeyword(Keyword keyword) {
        return keyword.name().charAt(0) + keyword.name().substring(1).toLowerCase().replace('_', ' ');
    }
}
