package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.Overture;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "FIN", collectorNumber = "284")
public class JidoorAristocraticCapital extends Card {

    public JidoorAristocraticCapital() {
        setBackFaceCard(new Overture());
        addCastingOption(new AdventureCast("{4}{U}{U}"));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }

    @Override
    public String getBackFaceClassName() {
        return "Overture";
    }
}
