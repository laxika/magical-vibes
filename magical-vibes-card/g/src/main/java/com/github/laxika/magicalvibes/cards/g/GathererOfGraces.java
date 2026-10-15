package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "85")
public class GathererOfGraces extends Card {

    public GathererOfGraces() {
        AttachmentsOnSource aurasAttached = new AttachmentsOnSource(true, false);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(aurasAttached, aurasAttached));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.AURA),
                                "an Aura",
                                false),
                        new RegenerateEffect()),
                "Sacrifice an Aura: Regenerate this creature."
        ));
    }
}
