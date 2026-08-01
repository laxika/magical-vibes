package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "174")
public class JaradGolgariLichLord extends Card {

    public JaradGolgariLichLord() {
        // Jarad gets +1/+1 for each creature card in your graveyard.
        CardsInGraveyard creatureCards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(creatureCards, creatureCards));

        // {1}{B}{G}, Sacrifice another creature: Each opponent loses life equal to the
        // sacrificed creature's power.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{G}",
                List.of(
                        new SacrificeCreatureCost(false, true, false, true),
                        new LoseLifeEffect(new XValue(), LoseLifeRecipient.EACH_OPPONENT)
                ),
                "{1}{B}{G}, Sacrifice another creature: Each opponent loses life equal to the "
                        + "sacrificed creature's power."
        ));

        // Sacrifice a Swamp and a Forest: Return this card from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                        new PermanentHasSubtypePredicate(CardSubtype.FOREST)
                                ),
                                List.of("a Swamp", "a Forest")
                        ),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "Sacrifice a Swamp and a Forest: Return this card from your graveyard to your hand."
        ));
    }
}
