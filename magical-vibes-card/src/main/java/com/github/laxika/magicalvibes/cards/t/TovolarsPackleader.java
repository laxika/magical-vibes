package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

public class TovolarsPackleader extends Card {

    public TovolarsPackleader() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, wolfTokens());
        addEffect(EffectSlot.ON_ATTACK, wolfTokens());
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));

        TargetFilter wolfOrWerewolfYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF)),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                )),
                "First target must be another Wolf or Werewolf you control");
        TargetFilter creatureYouDoNotControl = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Second target must be a creature you don't control");
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}",
                List.of(new FightTargetsEffect()),
                "{2}{G}{G}: Another target Wolf or Werewolf you control fights target creature you don't control.",
                List.of(wolfOrWerewolfYouControl, creatureYouDoNotControl),
                2,
                2
        ));
    }

    private CreateTokenEffect wolfTokens() {
        return new CreateTokenEffect(
                2, "Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of());
    }
}
