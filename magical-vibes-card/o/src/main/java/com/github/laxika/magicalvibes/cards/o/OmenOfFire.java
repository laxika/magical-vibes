package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "75")
public class OmenOfFire extends Card {

    public OmenOfFire() {
        // Return all Islands to their owners' hands.
        addEffect(EffectSlot.SPELL,
                ReturnToHandEffect.allPermanentsMatching(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));

        // Each player sacrifices a Plains or a white permanent of their choice for each white
        // permanent they control. A Plains is colorless (CR 202.2), so it never counts toward the
        // number sacrificed — it is only ever an eligible sacrifice.
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new PermanentCount(new PermanentColorInPredicate(Set.of(CardColor.WHITE)), CountScope.CONTROLLER),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.PLAINS),
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)))),
                SacrificeRecipient.EACH_PLAYER,
                true));
    }
}
