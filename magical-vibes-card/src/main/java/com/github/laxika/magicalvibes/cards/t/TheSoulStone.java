package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsHarnessed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.effect.HarnessEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "66")
@CardRegistration(set = "SPM", collectorNumber = "242")
@CardRegistration(set = "SPM", collectorNumber = "243")
public class TheSoulStone extends Card {

    public TheSoulStone() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}{B}",
                List.of(
                        new ExilePermanentCost(new PermanentIsCreaturePredicate(), "a creature"),
                        new HarnessEffect()
                ),
                "{6}{B}, {T}, Exile a creature you control: Harness The Soul Stone."
        ));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new SourceIsHarnessed(),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build()
        ));
    }
}
