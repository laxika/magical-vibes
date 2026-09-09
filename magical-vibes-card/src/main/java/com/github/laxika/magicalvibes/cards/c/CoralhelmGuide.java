package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "74")
public class CoralhelmGuide extends Card {

    public CoralhelmGuide() {
        addActivatedAbility(new ActivatedAbility(false, "{4}{U}", List.of(new MakeCreatureUnblockableEffect()),
                "{4}{U}: Target creature can't be blocked this turn.", TargetFilters.creature()));
    }
}
