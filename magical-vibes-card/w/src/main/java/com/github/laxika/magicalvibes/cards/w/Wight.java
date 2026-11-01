package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "127")
public class Wight extends Card {

    public Wight() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS),
                SequenceEffect.of(
                        new CreateTokenEffect(1, "Zombie", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), true),
                        new ExileTriggeringCardFromGraveyardEffect()));
    }
}
