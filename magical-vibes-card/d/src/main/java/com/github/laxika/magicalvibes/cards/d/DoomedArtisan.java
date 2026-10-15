package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "C19", collectorNumber = "3")
public class DoomedArtisan extends Card {

    public DoomedArtisan() {
        PermanentHasSubtypePredicate sculpture = new PermanentHasSubtypePredicate(CardSubtype.SCULPTURE);
        PermanentCount sculpturesYouControl = new PermanentCount(sculpture, CountScope.CONTROLLER);

        // Sculptures you control can't attack or block.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantAttackOrBlockEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(), sculpture)),
                "Sculptures you control can't attack or block"));

        // At the beginning of your end step, create a Sculpture whose P/T equal the number of
        // Sculptures you control.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new CreateTokenEffect(
                CardType.CREATURE, 1, "Sculpture", 0, 0, null, null,
                List.of(CardSubtype.SCULPTURE), Set.of(), Set.of(CardType.ARTIFACT), false, false,
                Map.of(EffectSlot.STATIC,
                        new SetPowerToughnessToAmountEffect(sculpturesYouControl, sculpturesYouControl)),
                List.of(), false, false, false, 0, Set.of()));
    }
}
