package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChosenNameSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfEachLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "53")
public class MedomaisProphecy extends Card {

    public MedomaisProphecy() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ScryEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new ChooseCardNameAtResolutionEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ChosenNameSpellCastTriggerEffect(List.of(new DrawCardEffect(2))));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new LookAtTopCardOfEachLibraryEffect());
    }
}
