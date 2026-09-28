package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetAttackingCreatureThenRevealUntilCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetAttackingCreatureThenRevealUntilCreatureToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetAttackingCreatureThenRevealUntilCreatureToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (ExileTargetAttackingCreatureThenRevealUntilCreatureToBattlefieldEffect) effect;
        UUID targetId = entry.targetsForEffect(typedEffect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        UUID targetControllerId = target == null
                ? null : gameQueryService.findPermanentController(gameData, targetId);
        if (target == null
                || targetControllerId == null
                || !entry.getControllerId().equals(targetControllerId)
                || !target.getCard().hasType(CardType.CREATURE)
                || !target.isAttacking()
                || targetId.equals(entry.getSourcePermanentId())
                || !permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));

        StackEntry revealEntry = new StackEntry(entry);
        revealEntry.setControllerId(targetControllerId);
        revealHandler.resolve(gameData, revealEntry,
                RevealUntilCardPredicateRestOnBottomRandomEffect.tappedAndAttacking(
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
