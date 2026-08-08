package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "34")
public class ResplendentAngel extends Card {

    public ResplendentAngel() {
        // At the beginning of each end step, if you gained 5 or more life this turn,
        // create a 4/4 white Angel creature token with flying and vigilance.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(5),
                new CreateTokenEffect("Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of())));

        // {3}{W}{W}{W}: Until end of turn, this creature gets +2/+2 and gains lifelink.
        addActivatedAbility(new ActivatedAbility(false, "{3}{W}{W}{W}",
                List.of(new BoostSelfEffect(2, 2), new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "{3}{W}{W}{W}: Until end of turn, this creature gets +2/+2 and gains lifelink."));
    }
}
