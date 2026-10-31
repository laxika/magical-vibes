package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "148")
@CardRegistration(set = "M14", collectorNumber = "228")
@CardRegistration(set = "EA2", collectorNumber = "23")
@CardRegistration(set = "EOS", collectorNumber = "28")
@CardRegistration(set = "EOS", collectorNumber = "73")
@CardRegistration(set = "EOS", collectorNumber = "118")
@CardRegistration(set = "EOS", collectorNumber = "163")
public class Mutavault extends Card {

    public Mutavault() {
        // {1}: This land becomes a 2/2 creature with all creature types until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AnimatePermanentsEffect(2, 2, CardSubtype.creatureTypes(), Set.of())),
                "{1}: This land becomes a 2/2 creature with all creature types until end of turn. It's still a land."
        ));
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
