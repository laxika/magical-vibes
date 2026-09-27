package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1501")
public class LaraCroftTombRaider extends Card {

    public LaraCroftTombRaider() {
        CardPredicate legendaryArtifactOrLand = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.LAND))
        )));
        addEffect(EffectSlot.ON_ATTACK,
                new ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffect(
                        legendaryArtifactOrLand));
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new Raid(), CreateTokenEffect.ofTreasureToken(1)));
    }
}
