package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "M21", collectorNumber = "104")
public class HoodedBlightfang extends Card {

    public HoodedBlightfang() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                        new GainLifeEffect(1)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                        new DestroyTargetPermanentEffect()));
    }
}
