package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "259")
public class LairOfTheHydra extends Card {

    public LairOfTheHydra() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCount(2, new PermanentIsLandPredicate()),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{G}",
                List.of(new AnimatePermanentsEffect(
                        new XValue(), new XValue(), List.of(CardSubtype.HYDRA), Set.of(),
                        CardColor.GREEN, Set.of(), GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN,
                        null, Set.of())),
                "{X}{G}: Until end of turn, this land becomes an X/X green Hydra creature. It's still a land. X can't be 0."
        ).withMinimumXValue(1));
    }
}
