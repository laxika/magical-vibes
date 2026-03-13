package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "112")
public class ReassemblingSkeleton extends Card {

    public ReassemblingSkeleton() {
        // {1}{B}: Return Reassembling Skeleton from your graveyard to the battlefield tapped.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new ReturnCardFromGraveyardEffect(
                        GraveyardChoiceDestination.BATTLEFIELD,
                        new CardIsSelfPredicate(),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false,  // targetGraveyard
                        true,   // returnAll
                        false,  // thisTurnOnly
                        null,   // attachmentTarget
                        false,  // gainLifeEqualToManaValue
                        false,  // attachToSource
                        false,  // grantHaste
                        false,  // exileAtEndStep
                        false,  // requiresManaValueEqualsX
                        null,   // grantColor
                        null,   // grantSubtype
                        true    // enterTapped
                )),
                "{1}{B}: Return Reassembling Skeleton from your graveyard to the battlefield tapped."
        ));
    }
}
