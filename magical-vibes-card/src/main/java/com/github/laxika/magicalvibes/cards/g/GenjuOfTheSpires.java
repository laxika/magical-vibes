package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "105")
public class GenjuOfTheSpires extends Card {

    public GenjuOfTheSpires() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                "Target must be a Mountain"
        ));

        // {2}: Enchanted Mountain becomes a 6/1 red Spirit creature until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(
                new AnimatePermanentsEffect(6, 1, List.of(CardSubtype.SPIRIT), Set.of(),
                        CardColor.RED, Set.of(), GrantScope.ENCHANTED_PERMANENT, EffectDuration.UNTIL_END_OF_TURN)
        ), "{2}: Enchanted Mountain becomes a 6/1 red Spirit creature until end of turn. It's still a land."));

        // When enchanted Mountain is put into a graveyard, you may return this card from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new MayEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                        "Return Genju of the Spires from your graveyard to your hand?"));
    }
}
