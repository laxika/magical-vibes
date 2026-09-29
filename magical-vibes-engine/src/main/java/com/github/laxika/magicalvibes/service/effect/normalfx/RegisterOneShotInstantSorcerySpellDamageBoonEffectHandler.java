package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOneShotInstantSorcerySpellDamageBoonEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import org.springframework.stereotype.Component;

import java.util.List;

/** Registers Molten Impact's source-independent excess-damage boon. */
@Component
public class RegisterOneShotInstantSorcerySpellDamageBoonEffectHandler
        implements NormalEffectHandlerBean {

    private static final CardPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterOneShotInstantSorcerySpellDamageBoonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int damage = Math.max(0, entry.getEventValue());
        if (damage == 0) {
            return;
        }

        PermanentPredicate opponentCreatureOrPlaneswalker = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate())),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        TargetFilter targetFilter = new PermanentPredicateTargetFilter(
                opponentCreatureOrPlaneswalker,
                "Target must be a creature or planeswalker an opponent controls");

        gameData.queueDelayedAction(new DelayedControllerSpellCastTrigger(
                entry.getControllerId(),
                null,
                entry.getCard(),
                INSTANT_OR_SORCERY,
                null,
                List.of(new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                        damage, opponentCreatureOrPlaneswalker)),
                true,
                false,
                targetFilter,
                null,
                null,
                false,
                true,
                gameData.turnNumber
        ));
    }
}
