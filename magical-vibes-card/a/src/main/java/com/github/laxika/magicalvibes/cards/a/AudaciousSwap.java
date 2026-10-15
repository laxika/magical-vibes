package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "NCC", collectorNumber = "44")
@CardRegistration(set = "NCC", collectorNumber = "145")
public class AudaciousSwap extends Card {

    public AudaciousSwap() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfCasualtyPaidEffect());
        addEffect(EffectSlot.SPELL, new CasualtyCost(2));
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsEnchantmentPredicate()),
                "Target must be a nonenchantment permanent")).addEffect(EffectSlot.SPELL,
                new ShuffleTargetPermanentIntoLibraryEffect(
                        new ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect(),
                        ThenEffectRecipient.TARGET_OWNER));
    }
}
