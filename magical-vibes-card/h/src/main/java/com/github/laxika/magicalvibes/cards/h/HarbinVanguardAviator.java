package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "212")
public class HarbinVanguardAviator extends Card {

    public HarbinVanguardAviator() {
        addTrigger(Triggers.youAttack(), new ConditionalEffect(
                new MinimumAttackingCreaturesOfSubtype(5, CardSubtype.SOLDIER),
                SequenceEffect.of(
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES))));
    }
}
