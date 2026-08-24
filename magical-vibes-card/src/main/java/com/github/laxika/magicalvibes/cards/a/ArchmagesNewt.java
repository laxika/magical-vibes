package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "39")
public class ArchmagesNewt extends Card {

    public ArchmagesNewt() {
        Set<CardType> instantOrSorcery = Set.of(CardType.INSTANT, CardType.SORCERY);
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ConditionalReplacementEffect(
                new SourceIsSaddled(),
                new GrantFlashbackToTargetGraveyardCardEffect(instantOrSorcery),
                new GrantFlashbackToTargetGraveyardCardEffect(instantOrSorcery, true)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(3), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 3",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
