package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "12")
public class KamiOfTheHonoredDead extends Card {

    public KamiOfTheHonoredDead() {
        // "Whenever this creature is dealt damage, you gain that much life." The damage amount is
        // snapshotted onto the trigger entry's event value, so EventValue gains exactly that much.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new GainLifeEffect(new EventValue()));

        // Soulshift 6: "When this creature dies, you may return target Spirit card with mana value 6
        // or less from your graveyard to your hand." Declining the graveyard choice is the "you may".
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardMaxManaValuePredicate(6))))
                .targetGraveyard(true)
                .build());
    }
}
