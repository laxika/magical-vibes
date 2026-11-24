package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseDamageSourceEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "WTH", collectorNumber = "96")
public class DesperateGambit extends Card {

    public DesperateGambit() {
        addSpellEffect(ChooseDamageSourceEffect.youControl());
        addSpellEffect(new FlipCoinWinEffect(
                PreventNextDamageByTargetCreatureEffect.doubling(Subjects.thisWay(StepKind.CHOOSE)),
                PreventNextDamageByTargetCreatureEffect.preventing(Subjects.thisWay(StepKind.CHOOSE))));
    }
}
