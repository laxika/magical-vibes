package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreaturesBlockingEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Resolves Mirror Match's copies of creatures attacking its controller. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfAttackingCreaturesBlockingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final CombatBlockService combatBlockService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfAttackingCreaturesBlockingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> attackers = gameData.orderedPlayerIds.stream()
                .flatMap(playerId -> gameData.playerBattlefields
                        .getOrDefault(playerId, List.of()).stream())
                .filter(permanent -> permanent.isAttacking()
                        && gameQueryService.isCreature(gameData, permanent)
                        && attacksControllerOrTheirPlaneswalker(gameData, permanent, controllerId))
                .toList();

        if (attackers.isEmpty()) {
            return;
        }
        // The copies are created as one event whose replacements the caster orders (CR 616.1), so each sees the
        // others enter (CR 603.6a). Each copy, doubled copies included, blocks the creature it copies; a token a
        // replacement adds or substitutes (Queen Allenal's Soldier, Jinnie Fay's Cat or Dog) follows the creating
        // effect's instructions too, so it blocks the first attacker.
        Map<Card, UUID> attackerByCard = new IdentityHashMap<>();
        attackers.forEach(attacker -> attackerByCard.put(attacker.getCard(), attacker.getId()));
        UUID firstAttackerId = attackers.getFirst().getId();
        Map<UUID, UUID> blockedAttackerByToken = new ConcurrentHashMap<>();
        tokenCopySupport.createTokenCopiesThen(gameData, entry,
                attackers.stream().map(Permanent::getCard).toList(), controllerId,
                CreateTokenCopyOfTargetPermanentEffect.exiledAtEndOfCombat(),
                (copiedCard, copy) -> blockedAttackerByToken.put(copy.getId(), attackerByCard.get(copiedCard)),
                (game, tokens) -> {
                    for (Permanent token : tokens) {
                        Permanent entered = gameQueryService.findPermanentById(game, token.getId());
                        if (entered == null || !gameQueryService.isCreature(game, entered)) {
                            continue;
                        }
                        combatBlockService.markTokenAsBlocking(game, entered, gameQueryService.findPermanentById(
                                game, blockedAttackerByToken.getOrDefault(token.getId(), firstAttackerId)));
                    }
                });
    }

    private boolean attacksControllerOrTheirPlaneswalker(GameData gameData, Permanent attacker,
                                                          UUID controllerId) {
        UUID attackTargetId = attacker.getAttackTarget();
        if (attackTargetId == null || gameData.playerIds.contains(attackTargetId)) {
            return controllerId.equals(attackTargetId);
        }

        Permanent attackTarget = gameQueryService.findPermanentById(gameData, attackTargetId);
        return attackTarget != null
                && gameQueryService.isPlaneswalker(gameData, attackTarget)
                && controllerId.equals(gameQueryService.findPermanentController(gameData, attackTargetId));
    }
}
