package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "13")
public class FamilyReunion extends Card {

    public FamilyReunion() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 until end of turn",
                        new BoostAllOwnCreaturesEffect(1, 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain hexproof until end of turn",
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_CREATURES))
        )));
    }
}
