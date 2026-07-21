package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.Fame;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Claim // Fame — front half (Claim).
 * Sorcery — Return target creature card with mana value 2 or less from your graveyard to the battlefield.
 * Back half (Fame) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "150")
public class ClaimFame extends Card {

    public ClaimFame() {
        Fame fame = new Fame();
        fame.setSetCode(getSetCode());
        fame.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(fame);

        // Return target creature card with mana value 2 or less from your graveyard to the battlefield.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(2)
                )))
                .targetGraveyard(true)
                .build());
    }

    @Override
    public String getBackFaceClassName() {
        return "Fame";
    }
}
