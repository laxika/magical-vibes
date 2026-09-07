package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "224")
public class TolsimirFriendToWolves extends Card {

    public TolsimirFriendToWolves() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE, 1, "Voja, Friend to Elves", 3, 3,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.WOLF), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, true, 0, Set.of()));

        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new TriggeringCardConditionalEffect(
                                new CardSubtypePredicate(CardSubtype.WOLF),
                                SequenceEffect.of(
                                        new GainLifeEffect(3),
                                        new EnteringCreatureFightsTargetCreatureEffect())));
    }
}
