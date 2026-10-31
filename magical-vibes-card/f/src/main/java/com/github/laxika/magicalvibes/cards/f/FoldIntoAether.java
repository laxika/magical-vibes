package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.condition.SpellCounteredThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldForTargetSpellControllerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "5DN", collectorNumber = "31")
public class FoldIntoAether extends Card {

    public FoldIntoAether() {
        // Counter target spell. If that spell is countered this way, its controller may put a creature
        // card from their hand onto the battlefield. The counter is listed first so the rider sees the
        // outcome of the counter (CR 608.2c).
        target(null, 1, 1)
                .addSpellEffect(new CounterSpellEffect())
                .addSpellEffect(new ConditionalEffect(
                        new SpellCounteredThisWay(),
                        new MayEffect(
                                new PutCardToBattlefieldForTargetSpellControllerEffect(
                                        new CardTypePredicate(CardType.CREATURE), "creature"),
                                "Put a creature card from your hand onto the battlefield?",
                                null,
                                MayChoicePlayer.TARGET_SPELL_CONTROLLER)));
    }
}
