package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "83")
public class WhirlerRogue extends Card {

    public WhirlerRogue() {
        // When this creature enters, create two 1/1 colorless Thopter artifact creature tokens with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)));

        // Tap two untapped artifacts you control: Target creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new MakeCreatureUnblockableEffect()),
                "Tap two untapped artifacts you control: Target creature can't be blocked this turn.",
                TargetFilters.creature()));
    }
}
