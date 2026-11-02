package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "237")
public class VeteranGuardmouse extends Card {

    public VeteranGuardmouse() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                new TriggeringSpellControllerConditionalEffect(new OncePerTurnTriggerEffect(
                        SequenceEffect.of(
                                new BoostSelfEffect(1, 0),
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF),
                                new ScryEffect(1)))));
    }
}
