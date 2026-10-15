package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "KLD", collectorNumber = "135")
public class StartYourEngines extends Card {

    public StartYourEngines() {
        addEffect(EffectSlot.SPELL, new GrantCardTypeToOwnPermanentsUntilEndOfTurnEffect(
                CardType.CREATURE, new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 0));
    }
}
