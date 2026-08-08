package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "66")
public class GenjuOfTheFens extends Card {

    public GenjuOfTheFens() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                "Target must be a Swamp"
        ));

        // {2}: Until end of turn, enchanted Swamp becomes a 2/2 black Spirit creature with
        // "{B}: This creature gets +1/+1 until end of turn." It's still a land.
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(
                new AnimatePermanentsEffect(2, 2, List.of(CardSubtype.SPIRIT), Set.of(), CardColor.BLACK,
                        Set.of(), GrantScope.ENCHANTED_PERMANENT, EffectDuration.UNTIL_END_OF_TURN),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(false, "{B}", List.of(new BoostSelfEffect(1, 1)),
                                "{B}: This creature gets +1/+1 until end of turn."),
                        GrantScope.ENCHANTED_PERMANENT, null, EffectDuration.UNTIL_END_OF_TURN)
        ), "{2}: Until end of turn, enchanted Swamp becomes a 2/2 black Spirit creature with "
                + "\"{B}: This creature gets +1/+1 until end of turn.\" It's still a land."));

        // When enchanted Swamp is put into a graveyard, you may return this card from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new MayEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                        "Return Genju of the Fens from your graveyard to your hand?"));
    }
}
