package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MKM", collectorNumber = "207")
@CardRegistration(set = "MKM", collectorNumber = "416")
public class IllTimedExplosion extends Card {

    public IllTimedExplosion() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(2, Subjects.you()),
                        new QueueReflexiveAbilityEffect(
                                DealDamageEffect.toEachCreature(new GreatestDiscardedCardManaValue()))),
                "Discard two cards?"));
    }
}
