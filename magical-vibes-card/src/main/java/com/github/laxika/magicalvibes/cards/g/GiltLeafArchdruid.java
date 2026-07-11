package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllLandsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "124")
public class GiltLeafArchdruid extends Card {

    public GiltLeafArchdruid() {
        // Whenever you cast a Druid spell, you may draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardSubtypePredicate(CardSubtype.DRUID),
                        List.of(new DrawCardEffect(1))),
                "Draw a card?"));

        // Tap seven untapped Druids you control: Gain control of all lands target player controls.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(7, new PermanentHasSubtypePredicate(CardSubtype.DRUID)),
                        new GainControlOfAllLandsTargetPlayerControlsEffect()),
                "Tap seven untapped Druids you control: Gain control of all lands target player controls."));
    }
}
