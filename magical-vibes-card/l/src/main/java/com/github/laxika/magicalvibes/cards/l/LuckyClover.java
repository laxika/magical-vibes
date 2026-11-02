package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "226")
public class LuckyClover extends Card {

    public LuckyClover() {
        addTrigger(Triggers.youCastSpell(),
                CopyControllerCastSpellOnSpellCastEffect.adventureCopy(new CardTruePredicate()));
    }
}
