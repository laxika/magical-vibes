package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "OTJ", collectorNumber = "164")
public class FullSteamAhead extends Card {

    public FullSteamAhead() {
        addSpellEffect(BoostEffect.creaturesYouControl(2, 2));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.otherCreaturesYouControl()));
        addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(StaticAbility.of(new CanBeBlockedByAtMostNCreaturesEffect(1)), Subjects.creaturesYouControl()));
    }
}
