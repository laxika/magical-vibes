package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "28")
@CardRegistration(set = "MKC", collectorNumber = "338")
public class ForebodingSteamboat extends Card {

    public ForebodingSteamboat() {
        PermanentPredicate eligibleCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))
        ));

        // When this Vehicle enters, each player chooses two nontoken, non-Vehicle creatures they control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffect(eligibleCreature, 2));

        // Whenever this Vehicle attacks, put a card exiled with it into its owner's graveyard. If you do, investigate.
        addEffect(EffectSlot.ON_ATTACK,
                new PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
