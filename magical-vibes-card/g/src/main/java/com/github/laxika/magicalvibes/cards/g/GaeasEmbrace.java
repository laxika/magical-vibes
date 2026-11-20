package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "255")
@CardRegistration(set = "VMA", collectorNumber = "211")
public class GaeasEmbrace extends Card {

    public GaeasEmbrace() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new RegenerateEffect()),
                "{G}: Regenerate enchanted creature."));
    }
}
