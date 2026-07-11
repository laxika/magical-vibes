package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "67")
public class MoongloveChangeling extends Card {

    public MoongloveChangeling() {
        // Changeling keyword auto-loads from Scryfall.

        // {B}: This creature gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                "{B}: This creature gains deathtouch until end of turn."));
    }
}
