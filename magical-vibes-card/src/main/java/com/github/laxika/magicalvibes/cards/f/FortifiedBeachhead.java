package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "262")
public class FortifiedBeachhead extends Card {

    public FortifiedBeachhead() {
        PermanentHasAnySubtypePredicate soldier = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER));

        // As this land enters, you may reveal a Soldier card from your hand. This land enters tapped
        // unless you revealed a Soldier card this way or you control a Soldier.
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(0, soldier),
                new RevealSubtypeOrEntersTappedEffect(CardSubtype.SOLDIER)));

        // {T}: Add {W} or {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        // {5}, {T}: Soldiers you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1, soldier)),
                "{5}, {T}: Soldiers you control get +1/+1 until end of turn."
        ));
    }
}
