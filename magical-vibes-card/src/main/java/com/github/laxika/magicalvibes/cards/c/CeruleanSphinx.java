package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "39")
public class CeruleanSphinx extends Card {

    public CeruleanSphinx() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new ShuffleSelfIntoOwnerLibraryEffect()),
                "{U}: This creature's owner shuffles it into their library."
        ));
    }
}
