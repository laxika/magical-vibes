package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.u.UnlikelyMeeting;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "61")
@CardRegistration(set = "WHO", collectorNumber = "666")
public class TwiceUponATimeUnlikelyMeeting extends Card {

    public TwiceUponATimeUnlikelyMeeting() {
        setBackFaceCard(new UnlikelyMeeting());
        addCastingOption(new AdventureCast("{2}{U}"));
        setCastCondition(new ControlsPermanentCount(2,
                new PermanentHasSubtypePredicate(CardSubtype.DOCTOR)));
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "UnlikelyMeeting";
    }
}
