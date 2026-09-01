package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "178")
public class NimbusMaze extends Card {

    public NimbusMaze() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add {W}. Activate only if you control an Island.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {W}. Activate only if you control an Island."
        ).withRequiredControlledPermanents(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), 1, "an Island"));

        // {T}: Add {U}. Activate only if you control a Plains.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE)),
                "{T}: Add {U}. Activate only if you control a Plains."
        ).withRequiredControlledPermanents(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS), 1, "a Plains"));
    }
}
