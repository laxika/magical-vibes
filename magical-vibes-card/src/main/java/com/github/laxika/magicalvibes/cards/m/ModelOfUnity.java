package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ModelOfUnityEffect;

@CardRegistration(set = "LTC", collectorNumber = "78")
@CardRegistration(set = "LTC", collectorNumber = "158")
public class ModelOfUnity extends Card {

    public ModelOfUnity() {
        addEffect(EffectSlot.ON_PLAYERS_FINISH_VOTING, new ModelOfUnityEffect());
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
