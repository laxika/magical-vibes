package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "236")
@CardRegistration(set = "HBG", collectorNumber = "279")
public class KaghaShadowArchdruid extends Card {

    public KaghaShadowArchdruid() {
        addTrigger(Triggers.attacks(),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF),
                new MillEffect(2, MillRecipient.CONTROLLER));
        addStaticEffect(new PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
                new CardIsPermanentPredicate(), null, true, false));
    }
}
