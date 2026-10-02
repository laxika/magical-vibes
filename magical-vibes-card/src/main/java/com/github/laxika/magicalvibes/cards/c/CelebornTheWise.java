package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsLookedAtWhileScrying;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HOC", collectorNumber = "203")
public class CelebornTheWise extends Card {

    public CelebornTheWise() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new MinimumMatchingAttackers(1, new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                new ScryEffect(1)));
        addEffect(EffectSlot.ON_CONTROLLER_SCRIES, new BoostSelfEffect(
                new CardsLookedAtWhileScrying(), new CardsLookedAtWhileScrying()));
    }
}
