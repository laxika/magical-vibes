package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "52")
public class BattlefieldPercher extends Card {

    public BattlefieldPercher() {
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                "creatures with flying"
        ));
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new BoostSelfEffect(1, 1)),
                "{1}{B}: Battlefield Percher gets +1/+1 until end of turn."));
    }
}
