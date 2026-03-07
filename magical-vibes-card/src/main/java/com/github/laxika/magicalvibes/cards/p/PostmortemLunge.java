package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "NPH", collectorNumber = "70")
public class PostmortemLunge extends Card {

    public PostmortemLunge() {
        addEffect(EffectSlot.SPELL, new ReturnCardFromGraveyardEffect(
                GraveyardChoiceDestination.BATTLEFIELD,
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                true,   // targetGraveyard
                false,  // returnAll
                false,  // thisTurnOnly
                null,   // attachmentTarget
                false,  // gainLifeEqualToManaValue
                false,  // attachToSource
                true,   // grantHaste
                true,   // exileAtEndStep
                true    // requiresManaValueEqualsX
        ));
    }
}
