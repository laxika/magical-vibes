package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "18")
@CardRegistration(set = "MIC", collectorNumber = "56")
public class CurseOfTheRestlessDead extends Card {

    public CurseOfTheRestlessDead() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.ENCHANTED_PLAYER_CONTROLS),
                CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
