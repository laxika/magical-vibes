package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CounterTriggeringSpellAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "143")
public class Dovescape extends Card {

    public Dovescape() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new CounterTriggeringSpellAndCreateTokensEffect(
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new EventValue(),
                                "Bird",
                                1,
                                1,
                                CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLUE),
                                List.of(CardSubtype.BIRD),
                                Set.of(Keyword.FLYING),
                                Set.of(),
                                false,
                                false,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of()))
        )));
    }
}
