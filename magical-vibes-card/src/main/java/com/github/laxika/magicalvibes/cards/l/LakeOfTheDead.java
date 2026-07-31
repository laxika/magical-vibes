package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAsEntersOrGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "140")
public class LakeOfTheDead extends Card {

    public LakeOfTheDead() {
        // If Lake of the Dead would enter, sacrifice a Swamp instead. If you do, put this land onto
        // the battlefield. If you don't, put it into its owner's graveyard.
        addEffect(EffectSlot.STATIC, new SacrificePermanentAsEntersOrGraveyardEffect(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                "a Swamp"));

        // {T}: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK)),
                "{T}: Add {B}."
        ));

        // {T}, Sacrifice a Swamp: Add {B}{B}{B}{B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                "Sacrifice a Swamp"),
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.BLACK)
                ),
                "{T}, Sacrifice a Swamp: Add {B}{B}{B}{B}."
        ));
    }
}
