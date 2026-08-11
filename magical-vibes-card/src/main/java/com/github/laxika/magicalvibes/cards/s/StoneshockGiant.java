package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "142")
public class StoneshockGiant extends Card {

    public StoneshockGiant() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{R}{R}",
                List.of(new MonstrosityEffect(3)),
                "{6}{R}{R}: Monstrosity 3."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));

        addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS, new CantBlockThisTurnEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                ))));
    }
}
