package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "43")
public class RayneAcademyChancellor extends Card {

    public RayneAcademyChancellor() {
        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"),
                new ConditionalEffect(new Enchanted(),
                        new MayEffect(new DrawCardEffect(), "Draw an additional card?")));
    }
}
