package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MassDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MassDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MassDamageEffect) effect;

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        // Source-relative amounts use the live source permanent when present, else the
        // last-known snapshot (matching LoseLifeEffectHandler).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int baseDamage = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        int damage = baseDamage;

        // Prefer the permanent's original (front-face) card id so PermanentIsSourceCardPredicate
        // correctly excludes the source on transform DFCs (back-face stack entry ≠ original id).
        UUID sourceCardId = source != null && source.getOriginalCard() != null
                ? source.getOriginalCard().getId()
                : entry.getCard().getId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCardId)
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(source);
        // The filter describes creatures only (e.g. "each creature without flying"); planeswalkers and
        // battles are damaged by their own flags whatever their keywords are.
        Predicate<Permanent> creatureFilter = p -> {
            if (gameQueryService.isCreature(gameData, p)) {
                return e.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext);
            }
            if (e.damagesPlaneswalkers() && p.getCard().hasType(CardType.PLANESWALKER)) {
                return !e.filterAppliesToPlaneswalkers() || e.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext);
            }
            return (e.damagesBattles() && p.getCard().hasType(CardType.BATTLE));
        };

        boolean previousExilesCreaturesDamaged = entry.isExilesCreaturesDamaged();
        entry.setExilesCreaturesDamaged(previousExilesCreaturesDamaged || e.exileInsteadOfDie());
        try {
            if (e.perCreatureAmount()) {
                // The amount describes the creature being damaged, so it is evaluated per creature with
                // that creature standing in as the amount's source permanent (Baki's Curse).
                damageSupport.damageAllCreaturesOnBattlefield(gameData, entry,
                        p -> amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, p)),
                        creatureFilter, e.exileInsteadOfDie(), e.cantRegenerate(), e.tapDamagedCreatures());
                return;
            }

            Map<UUID, Integer> damageBefore = e.exileInsteadOfDie()
                    ? new HashMap<>(gameData.damageDealtToPermanentsThisTurn) : Map.of();
            damageSupport.damageAllCreaturesOnBattlefield(gameData, entry, damage, creatureFilter,
                    e.exileInsteadOfDie(), e.cantRegenerate(), e.tapDamagedCreatures());
            if (e.exileInsteadOfDie() && e.damagesPlaneswalkers()) {
                markDamagedPlaneswalkersForExile(gameData, damageBefore);
            }

            if (e.damagesPlayers()) {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
                }
                gameOutcomeService.checkWinCondition(gameData);
            }
        } finally {
            entry.setExilesCreaturesDamaged(previousExilesCreaturesDamaged);
        }
    }

    /** Marks each planeswalker this effect dealt damage to so it is exiled instead of dying this turn. */
    private void markDamagedPlaneswalkersForExile(GameData gameData, Map<UUID, Integer> damageBefore) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().hasType(CardType.PLANESWALKER)
                        && gameData.damageDealtToPermanentsThisTurn.getOrDefault(permanent.getId(), 0)
                        > damageBefore.getOrDefault(permanent.getId(), 0)) {
                    permanent.setExileInsteadOfDieThisTurn(true);
                }
            }
        }
    }
}
