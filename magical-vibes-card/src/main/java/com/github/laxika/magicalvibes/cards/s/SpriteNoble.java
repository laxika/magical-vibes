package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "81")
public class SpriteNoble extends Card {

    public SpriteNoble() {
        var flying = new PermanentHasKeywordPredicate(Keyword.FLYING);

        // Other creatures you control with flying get +0/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES, flying));

        // {T}: Other creatures you control with flying get +1/+0 until end of turn.
        var otherFlying = new PermanentAllOfPredicate(
                List.of(flying, new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new BoostAllOwnCreaturesEffect(1, 0, otherFlying)),
                "{T}: Other creatures you control with flying get +1/+0 until end of turn."));
    }
}
