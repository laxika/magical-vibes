package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MIC", collectorNumber = "18")
@CardRegistration(set = "MIC", collectorNumber = "56")
public class CurseOfTheRestlessDead extends Card {

    public CurseOfTheRestlessDead() {
        addEffect(EffectSlot.ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD,
                CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
