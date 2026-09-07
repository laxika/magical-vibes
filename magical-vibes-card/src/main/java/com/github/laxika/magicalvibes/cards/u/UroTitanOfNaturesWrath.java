package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfUnlessEscapedEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "229")
public class UroTitanOfNaturesWrath extends Card {

    public UroTitanOfNaturesWrath() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfUnlessEscapedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, triggerEffect());
        addEffect(EffectSlot.ON_ATTACK, triggerEffect());

        addCastingOption(new GraveyardCast(null, "{G}{G}{U}{U}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)),
                null, false, false, true));
    }

    private static SequenceEffect triggerEffect() {
        return SequenceEffect.of(
                new GainLifeEffect(3),
                new DrawCardEffect(1),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                        "Put a land card from your hand onto the battlefield?"));
    }
}
