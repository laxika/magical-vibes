package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPM", collectorNumber = "68")
public class TheSpotsPortal extends Card {

    public TheSpotsPortal() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutTargetOnBottomOfLibraryEffect())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.VILLAIN))),
                        new LoseLifeEffect(2)));
    }
}
