package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "125")
public class UnholyHunger extends Card {

    public UnholyHunger() {
        // Destroy target creature.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard, you gain 2 life.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ))), new GainLifeEffect(2)));
    }
}
