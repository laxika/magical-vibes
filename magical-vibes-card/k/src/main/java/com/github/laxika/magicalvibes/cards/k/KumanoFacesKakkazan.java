package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EtchingOfKumano;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCountersToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "152")
public class KumanoFacesKakkazan extends Card {

    public KumanoFacesKakkazan() {
        setBackFaceCard(new EtchingOfKumano());

        addTrigger(Triggers.chapter(1),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(1), new DealDamageToEachMatchingPermanentEffect(
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                EachPermanentScope.ALL_PLAYERS));
        addTrigger(Triggers.chapter(2), new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new GrantAdditionalCountersToCastSpellEffect(new Fixed(1))),
                true, false));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "EtchingOfKumano";
    }
}
