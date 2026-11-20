package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;

@CardRegistration(set = "JOU", collectorNumber = "82")
public class SilenceTheBelievers extends Card {

    public SilenceTheBelievers() {
        setAdditionalManaCostPerExtraTarget("{2}{B}");

        target(TargetFilters.creature(), 0, 99)
                .addSpellEffect(new ExileTargetPermanentEffect(Subjects.allOf(
                        Subjects.attachmentsOf(Subjects.allTargets(), new PermanentHasSubtypePredicate(CardSubtype.AURA)),
                        Subjects.allTargets())));
    }
}
