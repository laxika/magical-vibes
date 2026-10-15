package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "208")
public class VisageOfBolas extends Card {

    public VisageOfBolas() {
        // When this artifact enters, you may search your library and/or graveyard for a card named
        // Nicol Bolas, the Deceiver, reveal it, and put it into your hand. If you search your
        // library this way, shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Nicol Bolas, the Deceiver"),
                "Search your library and/or graveyard for a card named Nicol Bolas, the Deceiver?"
        ));

        // {T}: Add {U}, {B}, or {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {U}, {B}, or {R}."
        ));
    }
}
