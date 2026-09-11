package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "229")
public class OrcusPrinceOfUndeath extends Card {

    public OrcusPrinceOfUndeath() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each other creature gets -X/-X until end of turn. You lose X life",
                        List.of(
                                new BoostAllCreaturesEffect(
                                        new Scaled(new XValue(), -1),
                                        new Scaled(new XValue(), -1),
                                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())),
                                new LoseLifeEffect(new XValue(), LoseLifeRecipient.CONTROLLER))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return up to X target creature cards with total mana value X or less from your graveyard to the battlefield. They gain haste until end of turn",
                        ReturnTargetCardsFromGraveyardToBattlefieldEffect.withinTotalManaValue(
                                new CardTypePredicate(CardType.CREATURE), new XValue(), true)
        ))));
    }
}
