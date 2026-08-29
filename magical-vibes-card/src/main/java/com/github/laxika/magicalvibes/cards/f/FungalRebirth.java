package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "182")
public class FungalRebirth extends Card {

    public FungalRebirth() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .build());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new Morbid(),
                new CreateTokenEffect(2, "Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
        ));
    }
}
