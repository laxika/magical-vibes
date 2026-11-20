package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "28")
public class OcularHalo extends Card {

    public OcularHalo() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new DrawCardEffect(1)),
                                "{T}: Draw a card."
                        ),
                        Subjects.attachedCreature()
                ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.attachedCreature())),
                "{W}: Enchanted creature gains vigilance until end of turn."
        ));
    }
}
