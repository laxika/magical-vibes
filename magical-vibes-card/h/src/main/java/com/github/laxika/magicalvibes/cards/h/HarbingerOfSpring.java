package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromNonSubtypeCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "128")
public class HarbingerOfSpring extends Card {

    public HarbingerOfSpring() {
        addEffect(EffectSlot.STATIC, new ProtectionFromNonSubtypeCreaturesEffect(CardSubtype.SPIRIT));
        // Soulshift 4: "When this creature dies, you may return target Spirit card with mana value 4
        // or less from your graveyard to your hand." The graveyard target is chosen as the trigger
        // goes on the stack; the return is optional when the trigger resolves.
        addEffect(EffectSlot.ON_DEATH, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardMaxManaValuePredicate(4))))
                .targetGraveyard(true)
                .build(), "Return the targeted Spirit card to your hand?"));
    }
}
