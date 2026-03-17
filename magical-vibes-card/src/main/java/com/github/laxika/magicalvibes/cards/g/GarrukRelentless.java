package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerDealDamageAndReceivePowerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "181")
public class GarrukRelentless extends Card {

    public GarrukRelentless() {
        // Set up back face
        GarrukTheVeilCursed backFace = new GarrukTheVeilCursed();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // When Garruk Relentless has two or fewer loyalty counters on him, transform him.
        // This is a state-triggered ability (MTG rule 603.8).
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) ->
                        sourcePermanent.getLoyaltyCounters() <= 2 && !sourcePermanent.isTransformed(),
                List.of(new TransformSelfEffect()),
                "Garruk Relentless's transform trigger"
        ));

        // 0: Garruk Relentless deals 3 damage to target creature.
        // That creature deals damage equal to its power to him.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new PlaneswalkerDealDamageAndReceivePowerDamageEffect(3)),
                "0: Garruk Relentless deals 3 damage to target creature. That creature deals damage equal to its power to him.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // 0: Create a 2/2 green Wolf creature token.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateCreatureTokenEffect("Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF),
                        Set.of(), Set.of())),
                "0: Create a 2/2 green Wolf creature token."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GarrukTheVeilCursed";
    }
}
