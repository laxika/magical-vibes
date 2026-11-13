package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "217")
@CardRegistration(set = "MKC", collectorNumber = "220")
public class Wavesifter extends Card {

    public Wavesifter() {
        // Evoke {G}{U}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{G}{U}"))));
        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());

        // When this creature enters, investigate twice.
        // Each investigation is a separate token creation event (Marneus Calgar triggers once per event).
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                CreateTokenEffect.ofClueToken(1), CreateTokenEffect.ofClueToken(1)));
    }
}
