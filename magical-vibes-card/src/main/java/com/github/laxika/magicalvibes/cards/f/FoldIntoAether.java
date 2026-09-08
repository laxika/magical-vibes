package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.condition.TargetSpellCanBeCountered;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldForTargetSpellControllerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "5DN", collectorNumber = "31")
public class FoldIntoAether extends Card {

    public FoldIntoAether() {
        target(null, 1, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetSpellCanBeCountered(),
                        new MayEffect(
                                new PutCardToBattlefieldForTargetSpellControllerEffect(
                                        new CardTypePredicate(CardType.CREATURE), "creature"),
                                "Put a creature card from your hand onto the battlefield?",
                                null,
                                MayChoicePlayer.TARGET_SPELL_CONTROLLER)))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
