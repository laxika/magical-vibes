package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "48")
public class Submerge extends Card {

    public Submerge() {
        addCastingOption(new AlternateHandCast(
                List.of(),
                new AllConditions(List.of(
                        new OpponentControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)))),
                false));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
    }
}
