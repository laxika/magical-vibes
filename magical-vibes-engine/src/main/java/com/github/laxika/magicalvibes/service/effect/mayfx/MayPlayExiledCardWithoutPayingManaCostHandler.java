package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Play an exiled card without paying its mana cost — Guile's counter replacement, Spell Queller's
 * leaves-the-battlefield trigger. Declining leaves the card exiled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayPlayExiledCardWithoutPayingManaCostHandler implements MayEffectHandlerBean {

    private final ExileFreeCastSupport exileFreeCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPlayExiledCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted && ability.targetCardId() != null) {
            if (grantsHaste(ability)) {
                var exiled = gameData.findExiledCard(ability.targetCardId());
                if (exiled != null && exiled.card().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
                    gameData.spellsGrantedHasteOnEntry.add(ability.targetCardId());
                }
            }
            if (isExclusive(ability)) {
                // "Cast a spell from among those cards" — one offer per exiled card was queued, so
                // withdraw the siblings; only a single spell may be cast (Shell of the Last Kappa).
                gameData.pendingMayAbilities.removeIf(pending -> pending != ability && isExclusive(pending));
            }
            boolean grantHaste = ability.effects().stream()
                    .filter(MayPlayExiledCardWithoutPayingManaCostEffect.class::isInstance)
                    .map(MayPlayExiledCardWithoutPayingManaCostEffect.class::cast)
                    .anyMatch(MayPlayExiledCardWithoutPayingManaCostEffect::grantHaste);
            exileFreeCastSupport.castFromExileWithoutPaying(
                    gameData, player, ability.targetCardId(), grantHaste);
            if (returnsSourceToHand(ability) && gameData.findExiledCard(ability.targetCardId()) == null) {
                returnSourceToHand(gameData, ability);
            }
        } else {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to play " , ability.sourceCard(), "."));
            log.info("Game {} - {} declines to play exiled {}", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    /** "If you do, return this permanent to its owner's hand" — the spell has left exile to be cast. */
    private void returnSourceToHand(GameData gameData, PendingMayAbility ability) {
        Permanent source = ability.sourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (source != null && permanentRemovalService.removePermanentToHand(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is returned to its owner's hand."));
        }
    }

    private boolean returnsSourceToHand(PendingMayAbility ability) {
        return ability.effects().stream()
                .anyMatch(effect -> effect instanceof MayPlayExiledCardWithoutPayingManaCostEffect e
                        && e.returnSourceToHand());
    }

    private boolean isExclusive(PendingMayAbility ability) {
        return ability.effects().stream()
                .anyMatch(effect -> effect instanceof MayPlayExiledCardWithoutPayingManaCostEffect e && e.exclusive());
    }

    private boolean grantsHaste(PendingMayAbility ability) {
        return ability.effects().stream()
                .anyMatch(effect -> effect instanceof MayPlayExiledCardWithoutPayingManaCostEffect e && e.grantHaste());
    }
}
