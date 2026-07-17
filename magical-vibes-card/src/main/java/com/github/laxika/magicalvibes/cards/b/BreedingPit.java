package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "148")
public class BreedingPit extends Card {

    public BreedingPit() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {B}{B}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{B}{B}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // At the beginning of your end step, create a 0/1 black Thrull creature token.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new CreateTokenEffect(
                "Thrull", 0, 1, CardColor.BLACK,
                List.of(CardSubtype.THRULL), Set.of(), Set.of()));
    }
}
