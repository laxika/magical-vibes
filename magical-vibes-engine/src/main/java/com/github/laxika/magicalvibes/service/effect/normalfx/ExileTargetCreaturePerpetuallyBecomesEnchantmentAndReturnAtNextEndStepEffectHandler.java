package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturePerpetuallyBecomesEnchantmentAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderControlEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreaturePerpetuallyBecomesEnchantmentAndReturnAtNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreaturePerpetuallyBecomesEnchantmentAndReturnAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getCard().getSpellTargets().size() == 1) {
            targetIds = entry.getTargetIds();
        }

        for (UUID targetId : targetIds) {
            if (targetId == null) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            List<Card> leavingCards = List.copyOf(target.cardsLeavingBattlefield());
            if (leavingCards.isEmpty() || !permanentRemovalService.removePermanentToExile(gameData, target)) {
                continue;
            }

            for (Card leavingCard : leavingCards) {
                ExiledCardEntry exiled = gameData.findExiledCard(leavingCard.getId());
                if (exiled == null) {
                    continue;
                }
                Card enchantment = leavingCard.createRuntimeCopy();
                enchantment.setType(CardType.ENCHANTMENT);
                enchantment.setAdditionalTypes(Set.of());
                enchantment.freeze();
                replaceExiledCard(gameData, exiled, enchantment);

                gameData.queueDelayedAction(new DelayedEndStepTrigger(
                        entry.getControllerId(),
                        entry.getCard(),
                        entry.getSourcePermanentId(),
                        null,
                        new ReturnExiledCardToBattlefieldUnderControlEffect(enchantment.getId())));
                gameLogService.append(gameData, GameLog.cardThen(enchantment,
                        " is exiled and perpetually becomes an enchantment. It will return at the beginning "
                                + "of the next end step under "
                                + gameData.playerIdToName.get(entry.getControllerId()) + "'s control."));
                log.info("Game {} - {} is exiled by {} and scheduled to return under controller's control",
                        gameData.id, enchantment.getName(), entry.getCard().getName());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void replaceExiledCard(GameData gameData, ExiledCardEntry original, Card replacement) {
        gameData.exiledCards.replaceAll(entry -> entry.card().getId().equals(original.card().getId())
                ? new ExiledCardEntry(replacement, original.ownerId(), original.sourcePermanentId(),
                original.faceDown(), original.exilerId(), original.exiledTurnNumber())
                : entry);
    }
}
