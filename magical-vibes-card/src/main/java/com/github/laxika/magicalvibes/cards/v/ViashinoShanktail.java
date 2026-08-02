package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "110")
public class ViashinoShanktail extends Card {

    public ViashinoShanktail() {
        // Bloodrush — {2}{R}, Discard this card: Target attacking creature gets +3/+1 and gains first strike until end of turn.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{R}",
                List.of(new BoostTargetCreatureEffect(3, 1),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                "Bloodrush — {2}{R}, Discard this card: Target attacking creature gets +3/+1 and gains first strike until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
