package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "319")
@CardRegistration(set = "WOE", collectorNumber = "373")
public class OgreChitterlord extends Card {

    public OgreChitterlord() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ratTrigger());
        addEffect(EffectSlot.ON_ATTACK, ratTrigger());
    }

    private static SequenceEffect ratTrigger() {
        PermanentPredicate rat = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.RAT)
        ));
        return SequenceEffect.of(
                ratToken(),
                new ConditionalEffect(
                        new ControlsPermanentCount(5, rat),
                        new BoostAllOwnCreaturesEffect(2, 0, rat))
        );
    }

    private static CreateTokenEffect ratToken() {
        return new CreateTokenEffect(
                2, "Rat", 1, 1, CardColor.BLACK, List.of(CardSubtype.RAT),
                Set.of(), Set.of(), Map.of(EffectSlot.STATIC, new CantBlockEffect()));
    }
}
