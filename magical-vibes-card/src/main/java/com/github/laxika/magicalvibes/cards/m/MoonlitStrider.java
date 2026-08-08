package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "16")
public class MoonlitStrider extends Card {

    public MoonlitStrider() {
        // The "you control" half rides on the target filter: the effect's targetSpec is evaluated
        // without a source permanent, so a controller predicate cannot work there.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new GrantProtectionChoiceUntilEndOfTurnEffect(false)),
                "Sacrifice Moonlit Strider: Target creature you control gains protection from the color of your choice until end of turn.",
                new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control")));

        // Soulshift 3: "When this creature dies, you may return target Spirit card with mana value 3
        // or less from your graveyard to your hand." The graveyard target is chosen as the trigger
        // goes on the stack; declining the choice is the "you may".
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardMaxManaValuePredicate(3))))
                .targetGraveyard(true)
                .build());
    }
}
