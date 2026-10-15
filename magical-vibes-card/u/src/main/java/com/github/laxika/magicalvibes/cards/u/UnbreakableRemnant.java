package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "2")
public class UnbreakableRemnant extends Card {

    public UnbreakableRemnant() {
        addCastingOption(new GraveyardCast(null, "{1}{W}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 2)), null,
                false, false, true));
        addEffect(EffectSlot.ON_SELF_CAST, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new PerpetuallyBoostOwnedCardsEffect(new CardNamedPredicate("Unbreakable Remnant"), 1, 1)));
    }
}
