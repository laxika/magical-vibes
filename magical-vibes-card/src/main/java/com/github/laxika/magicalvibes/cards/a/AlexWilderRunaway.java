package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureNotCastFromHandConditionalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "677")
public class AlexWilderRunaway extends Card {

    public AlexWilderRunaway() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureNotCastFromHandConditionalEffect(
                        new BoostEnteringCreatureEffect(2, 0, Set.of(Keyword.HASTE))));

        addCastingOption(new GraveyardCast(null, "{2}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 3)),
                null, false, false, true));
    }
}
