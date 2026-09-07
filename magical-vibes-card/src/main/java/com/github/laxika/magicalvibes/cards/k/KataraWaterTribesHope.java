package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "231")
public class KataraWaterTribesHope extends Card {

    public KataraWaterTribesHope() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Ally", 1, 1, CardColor.WHITE, List.of(CardSubtype.ALLY), Set.of(), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        WaterbendCost.x(),
                        new SetAllOwnCreaturesBasePowerToughnessEffect(new XValue(), new XValue())),
                "Waterbend {X}: Creatures you control have base power and toughness X/X until end of turn. X can't be 0.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ).withXValue().withMinimumXValue(1));
    }
}
