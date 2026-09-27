package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "20")
public class AceFlockbringer extends Card {

    public AceFlockbringer() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new OncePerTurnTriggerEffect(new SpellCastTriggerEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardNotPredicate(new CardKeywordPredicate(Keyword.FLYING)))),
                        List.of(new ConjureDuplicateOfTriggeringCreatureToHandEffect()))));
    }
}
