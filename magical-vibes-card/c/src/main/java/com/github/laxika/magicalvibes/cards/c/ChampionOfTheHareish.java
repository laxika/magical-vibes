package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BuddyListOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionOfTheHareishTriggerEffect;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "275")
@CardRegistration(set = "MB2", collectorNumber = "511")
public class ChampionOfTheHareish extends Card {

    public ChampionOfTheHareish() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BuddyListOnEnterEffect(List.of(CardSubtype.RABBIT, CardSubtype.SOLDIER)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new ChampionOfTheHareishTriggerEffect());
    }
}
