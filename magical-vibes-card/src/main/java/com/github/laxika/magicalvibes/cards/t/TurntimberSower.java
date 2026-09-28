package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "211")
public class TurntimberSower extends Card {

    public TurntimberSower() {
        // Whenever one or more land cards are put into your graveyard from anywhere, create a Plant.
        addEffect(EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new CreateTokenEffect("Plant", 0, 1, CardColor.GREEN,
                        List.of(CardSubtype.PLANT), Set.of(), Set.of()));

        // {G}, Sacrifice three creatures: Return target land card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificeMultiplePermanentsCost(3, new PermanentIsCreaturePredicate()),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.LAND))
                                .targetGraveyard(true)
                                .build()
                ),
                "{G}, Sacrifice three creatures: Return target land card from your graveyard to your hand."
        ));
    }
}
