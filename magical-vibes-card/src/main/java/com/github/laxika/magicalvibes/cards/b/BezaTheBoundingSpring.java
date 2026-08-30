package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreLifeThanController;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "2")
public class BezaTheBoundingSpring extends Card {

    public BezaTheBoundingSpring() {
        // Beza's comparisons are resolution-time conditions, not intervening-if ETB gates.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new AllOf(List.of(new OpponentControlsMoreLands())),
                CreateTokenEffect.ofTreasureToken(1)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new AnOpponentHasMoreLifeThanController(), new GainLifeEffect(4)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new OpponentControlsMoreCreatures(1),
                new CreateTokenEffect(2, "Fish", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.FISH), Set.of(), Set.of())));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new AnOpponentHasMoreCardsInHandThanController(), new DrawCardEffect(1)));
    }
}
