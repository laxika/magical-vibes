package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FUT", collectorNumber = "71")
public class MinionsMurmurs extends Card {

    public MinionsMurmurs() {
        PermanentCount creatures = new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new DrawCardEffect(creatures));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(creatures, LoseLifeRecipient.CONTROLLER));
    }
}
