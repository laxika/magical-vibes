package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.OpponentsWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "234")
public class TeysaOpulentOligarch extends Card {

    public TeysaOpulentOligarch() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                CreateTokenEffect.ofClueToken(new OpponentsWhoLostLifeThisTurn()));

        CreateTokenEffect spirit = new CreateTokenEffect(
                1, "Spirit", 1, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.CLUE),
                        new TriggeringPermanentControllerConditionalEffect(
                                new OncePerTurnTriggerEffect(spirit))));
    }
}
