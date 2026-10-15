package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringSpellOnBottomThenRevealEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "245")
@CardRegistration(set = "HBG", collectorNumber = "288")
public class NeeraWildMage extends Card {

    public NeeraWildMage() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new OncePerTurnTriggerEffect(new MayEffect(
                        new SpellCastTriggerEffect(
                                null,
                                List.of(new PutTriggeringSpellOnBottomThenRevealEffect(
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))))),
                        "Put it on the bottom of its owner's library?")));
    }
}
