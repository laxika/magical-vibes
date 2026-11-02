package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "44")
@CardRegistration(set = "NCC", collectorNumber = "145")
public class AudaciousSwap extends Card {

    public AudaciousSwap() {
        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfCasualtyPaidEffect());
        addCastingCost(new CasualtyCost(2));
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsEnchantmentPredicate()),
                "Target must be a nonenchantment permanent")).addSpellEffect(new ShuffleTargetPermanentIntoLibraryEffect(
                        new ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect(),
                        ThenEffectRecipient.TARGET_OWNER));
    }
}
