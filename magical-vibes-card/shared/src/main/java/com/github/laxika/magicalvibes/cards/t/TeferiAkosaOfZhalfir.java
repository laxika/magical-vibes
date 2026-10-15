package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfCreaturesThenReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

public class TeferiAkosaOfZhalfir extends Card {

    public TeferiAkosaOfZhalfir() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(2), new DiscardTwoUnlessCreatureEffect()),
                "+1: Draw two cards. Then discard two cards unless you discard a creature card."
        ));

        PermanentHasSubtypePredicate knight = new PermanentHasSubtypePredicate(CardSubtype.KNIGHT);
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateEmblemEffect(
                        List.of(
                                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES, knight),
                                new GrantTriggeredAbilityEffect(
                                        EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                                        new CounterUnlessPaysEffect(1),
                                        GrantScope.OWN_CREATURES,
                                        knight)
                        ),
                        "Knights you control get +1/+0 and have ward {1}."
                )),
                "−2: You get an emblem with \"Knights you control get +1/+0 and have ward {1}.\""
        ));

        PermanentAllOfPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentManaValueAtMostXPredicate()
        ));
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new TapAnyNumberOfCreaturesThenReflexiveAbilityEffect(
                        new ShuffleTargetPermanentIntoLibraryEffect(targetPredicate))),
                "−3: Tap any number of untapped creatures you control. When you do, shuffle target nonland permanent an opponent controls with mana value X or less into its owner's library, where X is the number of creatures tapped this way."
        ));
    }
}
