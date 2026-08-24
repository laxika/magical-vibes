package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "105")
public class FlittingGuerrilla extends Card {

    public FlittingGuerrilla() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new MillEffect(2, MillRecipient.CONTROLLER),
                new MillEffect(2, MillRecipient.EACH_OPPONENT),
                new MayEffect(
                        new ExileSourceCardFromGraveyardThenEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.TOP_OF_CONTROLLERS_LIBRARY)
                                        .filter(new CardAnyOfPredicate(List.of(
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardTypePredicate(CardType.BATTLE))))
                                        .targetGraveyard(true)
                                        .build()),
                        "Exile Flitting Guerrilla?")));
    }
}
