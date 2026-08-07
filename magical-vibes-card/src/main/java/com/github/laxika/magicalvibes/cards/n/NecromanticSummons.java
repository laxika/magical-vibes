package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "110")
public class NecromanticSummons extends Card {

    public NecromanticSummons() {
        // Put target creature card from a graveyard onto the battlefield under your control.
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // that creature enters with two additional +1/+1 counters on it.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .plusOneCountersIfCondition(new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))))
                .plusOneCounterCount(2)
                .build());
    }
}
