package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Takenuma Bleeder — {2}{B} Creature — Ogre Shaman 3/3.
 * <p>
 * "Whenever this creature attacks or blocks, you lose 1 life if you don't control a Demon."
 * One trigger per combat event, so the effect goes on both {@link EffectSlot#ON_ATTACK} and
 * {@link EffectSlot#ON_BLOCK}; the life loss is gated on not controlling a Demon.
 */
@CardRegistration(set = "BOK", collectorNumber = "86")
public class TakenumaBleeder extends Card {

    public TakenumaBleeder() {
        addEffect(EffectSlot.ON_ATTACK, bleedUnlessDemon());
        addEffect(EffectSlot.ON_BLOCK, bleedUnlessDemon());
    }

    private ConditionalEffect bleedUnlessDemon() {
        return new ConditionalEffect(
                new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DEMON))),
                new LoseLifeEffect(1));
    }
}
