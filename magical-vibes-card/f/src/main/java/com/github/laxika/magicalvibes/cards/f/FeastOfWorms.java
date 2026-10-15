package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "207")
public class FeastOfWorms extends Card {

    public FeastOfWorms() {
        // Destroy target land. If that land was legendary, its controller sacrifices another land
        // of their choice. The legendary check is last-known information read before destruction;
        // the sacrifice is routed to the destroyed land's controller. "Another" excludes the targeted
        // land itself, which is gone if destroyed and is still a candidate only if it survived.
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                EventStat.NONE,
                new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentIsTargetCardPredicate()))),
                        SacrificeRecipient.TARGET_PLAYER),
                ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET,
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
    }
}
