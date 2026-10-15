package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "FIN", collectorNumber = "283")
@CardRegistration(set = "FIN", collectorNumber = "310")
public class IshgardTheHolySee extends Card {

    public IshgardTheHolySee() {
        setBackFaceCard(new FaithAndGrief());
        addCastingOption(new AdventureCast("{3}{W}{W}"));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }

    @Override
    public String getBackFaceClassName() {
        return "FaithAndGrief";
    }
}
