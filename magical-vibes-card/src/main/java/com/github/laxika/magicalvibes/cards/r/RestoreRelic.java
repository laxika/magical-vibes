package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Restore Relic, the prepared spell of Lorehold Archivist (SOC 49). */
public class RestoreRelic extends Card {

    public RestoreRelic() {
        addEffect(EffectSlot.SPELL, new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE))),
                true,
                List.of(),
                false,
                false));
    }
}
