package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "132")
public class ReachOfBranches extends Card {

    public ReachOfBranches() {
        // Create a 2/5 green Treefolk Shaman creature token.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(1, "Treefolk Shaman", 2, 5, CardColor.GREEN,
                List.of(CardSubtype.TREEFOLK, CardSubtype.SHAMAN), Set.of(), Set.of()));

        // Whenever a Forest you control enters, you may return this card from your graveyard to your hand.
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.FOREST),
                        new MayEffect(ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                                "Return Reach of Branches from your graveyard to your hand?")));
    }
}
