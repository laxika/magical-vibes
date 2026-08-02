package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "114")
public class HeWhoHungers extends Card {

    public HeWhoHungers() {
        // Flying is auto-loaded from Scryfall keywords.
        // {1}, Sacrifice a Spirit: Target opponent reveals their hand. You choose a card from it.
        // That player discards that card. Activate only as a sorcery.
        // excludeSource=false — He Who Hungers is itself a Spirit and may be sacrificed to its own ability.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                                "Sacrifice a Spirit",
                                false),
                        new ChooseCardsFromTargetHandEffect(1, List.of(), HandChoiceDestination.DISCARD)),
                "{1}, Sacrifice a Spirit: Target opponent reveals their hand. You choose a card from it. "
                        + "That player discards that card. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null, null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        // Soulshift 4: "When this creature dies, you may return target Spirit card with mana value 4
        // or less from your graveyard to your hand."
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardMaxManaValuePredicate(4))))
                .targetGraveyard(true)
                .build());
    }
}
