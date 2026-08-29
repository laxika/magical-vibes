package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EachOpponentSacrificesPermanentUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentSacrificesPermanentUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var payOrSacrifice = (EachOpponentSacrificesPermanentUnlessPaysEffect) effect;
        UUID sourceControllerId = entry.getControllerId();
        for (UUID playerId : apnapOpponents(gameData, sourceControllerId)) {
            if (!hasSacrificeOption(gameData, playerId, sourceControllerId)) {
                continue;
            }
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    entry.getCard(), playerId, List.of(payOrSacrifice),
                    entry.getCard().getName() + " - Pay " + payOrSacrifice.manaCost()
                            + " or sacrifice a permanent?",
                    playerId, payOrSacrifice.manaCost(), entry.getSourcePermanentId(),
                    null, 0, 0, null, null, null, entry.getSourcePermanentSnapshot(),
                    sourceControllerId, null));
        }
    }

    public void handleChoice(GameData gameData, Player player, boolean accepted,
            PendingMayAbility ability, EachOpponentSacrificesPermanentUnlessPaysEffect effect) {
        UUID sacrificingPlayerId = ability.controllerId();
        UUID sourceControllerId = sourceControllerId(gameData, ability, sacrificingPlayerId);
        if (!hasSacrificeOption(gameData, sacrificingPlayerId, sourceControllerId)) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        boolean paid = false;
        if (accepted) {
            ManaCost cost = new ManaCost(effect.manaCost());
            ManaPool pool = gameData.playerManaPools.get(sacrificingPlayerId);
            if (pool != null && cost.canPay(pool)) {
                cost.pay(pool);
                paid = true;
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + effect.manaCost() + ". (",
                        ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} for {}", gameData.id, player.getUsername(),
                        effect.manaCost(), ability.sourceCard().getName());
            }
        }

        if (paid) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        SacrificePermanentsEffect sacrifice = new SacrificePermanentsEffect(
                1, new PermanentTruePredicate(), SacrificeRecipient.TARGET_PLAYER);
        StackEntry sacrificeEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
                ability.sourceCard().getName() + "'s ability", new ArrayList<>(List.of(sacrifice)),
                sacrificingPlayerId, ability.sourcePermanentId());
        sacrificeEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        sacrificePermanentsEffectHandler.resolve(gameData, sacrificeEntry, sacrifice);

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    private boolean hasSacrificeOption(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return false;
        }
        List<?> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && !battlefield.isEmpty();
    }

    private UUID sourceControllerId(GameData gameData, PendingMayAbility ability, UUID fallback) {
        if (ability.sourceControllerId() != null) {
            return ability.sourceControllerId();
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        return controllerId != null ? controllerId : fallback;
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID sourceControllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return ordered.stream().filter(playerId -> !playerId.equals(sourceControllerId)).toList();
    }
}
