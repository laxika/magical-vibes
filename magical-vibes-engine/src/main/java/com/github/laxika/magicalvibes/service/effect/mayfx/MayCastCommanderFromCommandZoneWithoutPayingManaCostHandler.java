package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles accepting or declining Geode Golem's command-zone free-cast offer. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastCommanderFromCommandZoneWithoutPayingManaCostHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final SpellCastingService spellCastingService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card card = ability.sourceCard();
        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to cast ", card, " from the command zone."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        List<Card> commandZone = gameData.playerCommandZones.get(player.getId());
        if (commandZone == null || commandZone.stream().noneMatch(c -> c.getId().equals(card.getId()))) {
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is no longer in the command zone."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (card.isCastOnlyFromGraveyard()
                || !gameQueryService.canCastSpellFromZone(gameData, card, Zone.COMMAND, player.getId())) {
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " can't be cast from the command zone."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // The waived mana cost does not waive commander tax (CR 903.8): the tax already owed
        // for earlier command-zone casts must still be paid, or the cast does not happen.
        if (gameData.commanderTaxByCardId.getOrDefault(card.getId(), 0) > 0) {
            Card paymentCard = card.createRuntimeCopy();
            paymentCard.setManaCost("{0}");
            try {
                spellCastingService.paySpellManaCostFromNonHandZone(gameData, player.getId(), paymentCard, 0,
                        Zone.COMMAND);
            } catch (IllegalArgumentException | IllegalStateException exception) {
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " cannot be cast because its commander tax cannot be paid."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        gameData.pendingMayAbilities.removeIf(pending -> pending != ability
                && pending.effects().stream().anyMatch(
                MayCastCommanderFromCommandZoneWithoutPayingManaCostEffect.class::isInstance));
        commandZone.removeIf(c -> c.getId().equals(card.getId()));
        mayCastHandlerService.castCardFromCommandZoneWithoutPaying(gameData, player, card);
    }
}
