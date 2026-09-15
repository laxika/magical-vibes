package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerCastThreeOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "SLX", collectorNumber = "13")
public class TheHowlingAbomination extends Card {

    public TheHowlingAbomination() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerCastThreeOrMoreSpellsThisTurn(new CardTruePredicate()),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));

        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, SequenceEffect.of(
                new BoostSelfEffect(2, 2),
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)));
    }
}
