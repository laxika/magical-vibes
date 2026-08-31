package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "297")
public class VoiceOfTheWoods extends Card {

    public VoiceOfTheWoods() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(5, new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                        new CreateTokenEffect("Elemental", 7, 7, CardColor.GREEN,
                                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.TRAMPLE), Set.of())
                ),
                "Tap five untapped Elves you control: Create a 7/7 green Elemental creature token with trample."
        ));
    }
}
