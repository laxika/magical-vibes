package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "89")
public class DarkDabbling extends Card {

    public DarkDabbling() {
        // Regenerate target creature. Draw a card.
        target(TargetFilters.creature());
        addEffect(EffectSlot.SPELL, new RegenerateEffect(true));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));

        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // also regenerate each other creature you control.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ))), new RegenerateAllOwnCreaturesEffect(null, true)));
    }
}
