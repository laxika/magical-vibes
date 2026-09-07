package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "235")
public class SamutTyrantSmasher extends Card {

    public SamutTyrantSmasher() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(
                        new BoostTargetCreatureEffect(2, 1),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        new ScryEffect(1)
                ),
                "-1: Target creature gets +2/+1 and gains haste until end of turn. Scry 1.",
                TargetFilters.creature()
        ));
    }
}
