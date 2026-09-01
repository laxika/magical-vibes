package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "8")
public class DawnEvangel extends Card {

    public DawnEvangel() {
        CardEffect returnEligibleCreature = new TriggeringPermanentConditionalEffect(
                new PermanentIsEnchantedBySourceControllerAuraPredicate(),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(2))))
                        .targetGraveyard(true)
                        .build());
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, returnEligibleCreature);
        addEffect(EffectSlot.ON_DEATH, returnEligibleCreature);
    }
}
