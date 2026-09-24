package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellUntilNonlandDealManaValueDifferenceAndMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Lady Loki's spell-exile, chaos-dig, damage, and free-cast trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringSpellUntilNonlandDealManaValueDifferenceAndMayCastEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringSpellUntilNonlandDealManaValueDifferenceAndMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        StackEntry triggeringSpell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> triggeringCardId.equals(candidate.getTargetableId()))
                .findFirst()
                .orElse(null);
        if (triggeringSpell == null) {
            return;
        }

        Card spellCard = triggeringSpell.getPhysicalCard();
        UUID spellOwnerId = triggeringSpell.getOwnerId();
        UUID castingPlayerId = triggeringSpell.getControllerId();
        gameData.stack.remove(triggeringSpell);
        exileService.exileCard(gameData, spellOwnerId, spellCard);
        gameLogService.append(gameData,
                GameLog.cardThen(spellCard, " is exiled by " + entry.getCard().getName() + "."));

        List<Card> deck = gameData.playerDecks.get(castingPlayerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        Card hit = null;
        while (!deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, castingPlayerId, topCard);
            if (!topCard.hasType(CardType.LAND)) {
                hit = topCard;
                break;
            }
        }

        if (hit == null) {
            return;
        }

        int spellManaValue = entry.getEventValue();
        int damageAmount = Math.abs(spellManaValue - hit.getManaValue());
        if (damageAmount > 0 && !damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, damageAmount, entry);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(entry.getControllerId())) {
                    damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
                }
            }
            gameOutcomeService.checkWinCondition(gameData);
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "Cast " + hit.getName() + " without paying its mana cost?",
                hit.getId()
        ));
        log.info("Game {} - {} may cast {} without paying its mana cost",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), hit.getName());
    }
}
