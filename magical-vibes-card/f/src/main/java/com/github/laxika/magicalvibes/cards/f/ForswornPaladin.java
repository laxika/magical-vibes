package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToActivate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "104")
public class ForswornPaladin extends Card {

    public ForswornPaladin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new PayLifeCost(1), CreateTokenEffect.ofTreasureToken(1)),
                "{1}{B}, {T}, Pay 1 life: Create a Treasure token."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new BoostTargetCreatureEffect(2, 0),
                        new ConditionalEffect(
                                new TreasureManaSpentToActivate(),
                                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET))),
                "{2}{B}: Target creature gets +2/+0 until end of turn. If mana from a Treasure was spent to activate this ability, that creature also gains deathtouch until end of turn.",
                TargetFilters.creature()));
    }
}
