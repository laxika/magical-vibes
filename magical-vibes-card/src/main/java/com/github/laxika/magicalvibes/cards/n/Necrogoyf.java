package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MH2", collectorNumber = "93")
public class Necrogoyf extends Card {

    public Necrogoyf() {
        CardsInGraveyard creaturesInAllGraveyards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                creaturesInAllGraveyards, new Fixed(4)));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DiscardEffect(1, DiscardRecipient.ACTIVE_PLAYER));
        addCastingOption(new MadnessCast("{1}{B}{B}"));
    }
}
