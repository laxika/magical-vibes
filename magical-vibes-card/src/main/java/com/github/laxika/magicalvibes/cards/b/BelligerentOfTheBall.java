package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "120")
public class BelligerentOfTheBall extends Card {

    public BelligerentOfTheBall() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(
                        new PermanentEnteredThisTurn(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2),
                        SequenceEffect.of(
                                new BoostTargetCreatureEffect(1, 0),
                                new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET))));
    }
}
