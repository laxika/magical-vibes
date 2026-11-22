package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOU", collectorNumber = "65")
public class HourOfGlory extends Card {

    public HourOfGlory() {
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(
                        HappenedThisWay.toA(StepKind.EXILE, new PermanentHasSubtypePredicate(CardSubtype.GOD)),
                        ExileCardsWithSameNameEffect.every(Subjects.target(),
                                Subjects.controllerOf(Subjects.target()), Zone.HAND)));
    }
}
