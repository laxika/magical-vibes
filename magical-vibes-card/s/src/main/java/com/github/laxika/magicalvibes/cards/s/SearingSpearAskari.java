package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "191")
public class SearingSpearAskari extends Card {

    public SearingSpearAskari() {
        // Flanking is auto-loaded from Scryfall.
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)), "{1}{R}: Searing Spear Askari gains menace until end of turn."));
    }
}
