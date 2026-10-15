package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftSlimefootThallidTransplantSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "26")
public class SlimefootThallidTransplant extends Card {

    public SlimefootThallidTransplant() {
        // Whenever a Swamp or Forest enters under your control, draft a card from this creature's
        // spellbook.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SWAMP),
                                new CardSubtypePredicate(CardSubtype.FOREST))),
                        new DraftSlimefootThallidTransplantSpellbookEffect()));
    }
}
