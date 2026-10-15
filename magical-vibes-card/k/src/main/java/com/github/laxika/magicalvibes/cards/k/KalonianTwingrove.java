package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "182")
public class KalonianTwingrove extends Card {

    public KalonianTwingrove() {
        PermanentCount forestsYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(forestsYouControl, forestsYouControl));

        // The token carries the same CDA, evaluated for its own controller.
        PermanentCount tokenForests =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1, "Treefolk Warrior", 0, 0,
                CardColor.GREEN, List.of(CardSubtype.TREEFOLK, CardSubtype.WARRIOR),
                Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(tokenForests, tokenForests))
        ));
    }
}
