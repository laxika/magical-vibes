package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "210")
public class OgnisTheDragonsLash extends Card {

    public OgnisTheDragonsLash() {
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.HASTE),
                        CreateTokenEffect.ofTappedTreasureToken(1)));
    }
}
