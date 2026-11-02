package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "28")
@CardRegistration(set = "OPCA", collectorNumber = "58")
public class OnakkeCatacomb extends Card {

    public OnakkeCatacomb() {
        addStaticEffect(new GrantColorEffect(CardColor.BLACK, GrantScope.ALL_CREATURES, true));
        addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_CREATURES));
        addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                new BoostAllOwnCreaturesEffect(1, 0),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ALL_OWN_CREATURES)));
    }
}
