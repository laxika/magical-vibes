package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "9ED", collectorNumber = "242")
@CardRegistration(set = "LEA", collectorNumber = "194")
@CardRegistration(set = "ME4", collectorNumber = "154")
@CardRegistration(set = "LEB", collectorNumber = "195")
@CardRegistration(set = "2ED", collectorNumber = "195")
@CardRegistration(set = "5ED", collectorNumber = "294")
@CardRegistration(set = "4ED", collectorNumber = "245")
@CardRegistration(set = "BTD", collectorNumber = "56")
@CardRegistration(set = "SUM", collectorNumber = "196")
@CardRegistration(set = "3ED", collectorNumber = "196")
public class ForceOfNature extends Card {

    public ForceOfNature() {
        // At the beginning of your upkeep, this creature deals 8 damage to you unless you pay {G}{G}{G}{G}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{G}{G}{G}{G}", DealDamageEffect.to(Subjects.you(), 8)));
    }
}
