package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "50")
public class ExhumerThrull extends Card {

    public ExhumerThrull() {
        ReturnTargetCardsFromGraveyardToHandEffect returnCreature =
                ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                        new CardTypePredicate(CardType.CREATURE));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, returnCreature);
        addTrigger(Triggers.dies(ObjectRelation.HAUNTED), returnCreature);

        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.THIS), new HauntEffect());
    }
}
