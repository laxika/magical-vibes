package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "48")
@CardRegistration(set = "MSC", collectorNumber = "356")
public class AliciaMastersSkilledSculptor extends Card {

    public AliciaMastersSkilledSculptor() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                CreateTokenEffect.ofTreasureToken(1)));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new EachPlayerGainsControlOfOwnedPermanentsMatchingEffect(new PermanentIsCreaturePredicate()));
    }
}
