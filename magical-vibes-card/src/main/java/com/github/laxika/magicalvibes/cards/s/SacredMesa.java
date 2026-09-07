package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "37")
@CardRegistration(set = "ATH", collectorNumber = "13")
@CardRegistration(set = "TSB", collectorNumber = "13")
public class SacredMesa extends Card {

    public SacredMesa() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you sacrifice a Pegasus.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.PEGASUS), "a Pegasus"));

        // {1}{W}: Create a 1/1 white Pegasus creature token with flying.
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new CreateTokenEffect("Pegasus", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.PEGASUS), Set.of(Keyword.FLYING), Set.of())),
                "{1}{W}: Create a 1/1 white Pegasus creature token with flying."));
    }
}
