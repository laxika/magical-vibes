package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "39")
@CardRegistration(set = "RVR", collectorNumber = "36")
public class CeruleanSphinx extends Card {

    public CeruleanSphinx() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(ShuffleTargetPermanentIntoLibraryEffect.of(Subjects.source())),
                "{U}: This creature's owner shuffles it into their library."
        ));
    }
}
