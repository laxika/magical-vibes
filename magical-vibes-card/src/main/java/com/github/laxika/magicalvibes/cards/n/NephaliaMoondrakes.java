package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "75")
public class NephaliaMoondrakes extends Card {

    public NephaliaMoondrakes() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES)
                ),
                "{4}{U}{U}, Exile this card from your graveyard: Creatures you control gain flying until end of turn."
        ));
    }
}
