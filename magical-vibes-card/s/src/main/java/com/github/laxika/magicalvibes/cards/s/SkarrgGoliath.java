package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "133")
public class SkarrgGoliath extends Card {

    public SkarrgGoliath() {
        // Trample is auto-loaded from Scryfall.
        // Bloodrush — {5}{G}{G}, Discard this card: Target attacking creature gets
        // +9/+9 and gains trample until end of turn. Discard cost is intrinsic to the
        // hand ability; the engine pays it.
        addHandActivatedAbility(new ActivatedAbility(false, "{5}{G}{G}",
                List.of(new BoostTargetCreatureEffect(9, 9),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                "Bloodrush — {5}{G}{G}, Discard this card: Target attacking creature gets +9/+9 and gains trample until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
