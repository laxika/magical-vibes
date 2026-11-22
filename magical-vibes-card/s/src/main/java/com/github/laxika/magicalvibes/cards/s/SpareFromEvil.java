package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromNonSubtypeCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ISD", collectorNumber = "34")
public class SpareFromEvil extends Card {

    public SpareFromEvil() {
        addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                StaticAbility.of(new ProtectionFromNonSubtypeCreaturesEffect(CardSubtype.HUMAN)),
                Subjects.creaturesYouControl()));
    }
}
