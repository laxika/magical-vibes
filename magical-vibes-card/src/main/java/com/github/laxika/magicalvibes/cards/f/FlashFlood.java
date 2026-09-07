package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHR", collectorNumber = "21")
@CardRegistration(set = "LEG", collectorNumber = "57")
public class FlashFlood extends Card {

    public FlashFlood() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target red permanent",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentColorInPredicate(Set.of(CardColor.RED)),
                                "Target must be a red permanent.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target Mountain to its owner's hand",
                        ReturnToHandEffect.target(),
                        new PermanentPredicateTargetFilter(
                                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                                "Target must be a Mountain."))
        )));
    }
}
