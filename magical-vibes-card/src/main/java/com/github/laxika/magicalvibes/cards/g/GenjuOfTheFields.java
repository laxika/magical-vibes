package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "5")
public class GenjuOfTheFields extends Card {

    public GenjuOfTheFields() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS),
                "Target must be a Plains"
        ));

        // {2}: Until end of turn, enchanted Plains becomes a 2/5 white Spirit creature with
        // "Whenever this creature deals damage, its controller gains that much life." It's still a land.
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(
                new AnimatePermanentsEffect(2, 5, List.of(CardSubtype.SPIRIT), Set.of(), CardColor.WHITE,
                        Set.of(), GrantScope.ENCHANTED_PERMANENT, EffectDuration.UNTIL_END_OF_TURN),
                GrantEffectToTargetEffect.toEnchantedPermanent(EffectSlot.ON_SELF_DEALS_DAMAGE,
                        new GainLifeEffect(new EventValue()), EffectDuration.UNTIL_END_OF_TURN)
        ), "{2}: Until end of turn, enchanted Plains becomes a 2/5 white Spirit creature with "
                + "\"Whenever this creature deals damage, its controller gains that much life.\" It's still a land."));

        // When enchanted Plains is put into a graveyard, you may return this card from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new MayEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                        "Return Genju of the Fields from your graveyard to your hand?"));
    }
}
