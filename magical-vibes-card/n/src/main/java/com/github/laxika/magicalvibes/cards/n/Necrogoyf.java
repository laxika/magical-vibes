package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "93")
@CardRegistration(set = "M3C", collectorNumber = "202")
public class Necrogoyf extends Card {

    public Necrogoyf() {
        CardsInGraveyard creaturesInAllGraveyards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.ANY_PLAYER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                creaturesInAllGraveyards, new Fixed(4)));
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DiscardEffect(1, DiscardRecipient.ACTIVE_PLAYER));
        addCastingOption(new MadnessCast("{1}{B}{B}"));
    }
}
