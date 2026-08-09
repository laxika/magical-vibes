package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpendBlueManaAsAnyColorForActivatedAbilitiesEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "47")
public class QuicksilverElemental extends Card {

    public QuicksilverElemental() {
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new GainActivatedAbilitiesOfTargetCreatureUntilEndOfTurnEffect()),
                "{U}: This creature gains all activated abilities of target creature until end of turn."));
        addEffect(EffectSlot.STATIC, new SpendBlueManaAsAnyColorForActivatedAbilitiesEffect());
    }
}
