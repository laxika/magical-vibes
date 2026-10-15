package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RestoreRelic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Lorehold Archivist // Restore Relic (SOC 49). */
@CardRegistration(set = "SOC", collectorNumber = "49")
@CardRegistration(set = "SOC", collectorNumber = "97")
public class LoreholdArchivistRestoreRelic extends Card {

    public LoreholdArchivistRestoreRelic() {
        setBackFaceCard(new RestoreRelic());

        // At the beginning of your upkeep, if there are three or more artifact and/or creature
        // cards in your graveyard, this creature becomes prepared.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new GraveyardCardThreshold(3, new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE)))),
                new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "RestoreRelic";
    }
}
