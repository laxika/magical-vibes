package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C18", collectorNumber = "28")
public class VarchildBetrayerOfKjeldor extends Card {

    public VarchildBetrayerOfKjeldor() {
        PermanentAllOfPredicate opposingSurvivors = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.SURVIVOR),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenForTargetPlayerEffect(new CreateTokenEffect(
                        new EventValue(), "Survivor", 1, 1, CardColor.RED,
                        List.of(CardSubtype.SURVIVOR), Set.of(), Set.of())));
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                opposingSurvivors, new PermanentTruePredicate(),
                "Survivors your opponents control can't block"));
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentNotPredicate(opposingSurvivors), true));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new GainControlOfAllPermanentsMatchingEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SURVIVOR),
                        ControlDuration.PERMANENT));
    }
}
