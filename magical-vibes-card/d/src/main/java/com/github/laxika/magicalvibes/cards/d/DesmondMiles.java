package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ACR", collectorNumber = "24")
@CardRegistration(set = "ACR", collectorNumber = "130")
public class DesmondMiles extends Card {

    public DesmondMiles() {
        PermanentCount otherAssassinsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN),
                CountScope.CONTROLLER,
                true
        );
        CardsInGraveyard assassinCardsInYourGraveyard = new CardsInGraveyard(
                new CardSubtypePredicate(CardSubtype.ASSASSIN),
                CountScope.CONTROLLER
        );
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new Sum(otherAssassinsYouControl, assassinCardsInYourGraveyard),
                new Fixed(0)
        ));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new SurveilEffect(new EventValue()));
    }
}
