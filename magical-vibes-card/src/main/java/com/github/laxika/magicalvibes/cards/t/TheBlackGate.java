package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreatureCantBeBlockedByMostLifePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "47")
@CardRegistration(set = "HOC", collectorNumber = "87")
@CardRegistration(set = "LTC", collectorNumber = "80")
@CardRegistration(set = "LTC", collectorNumber = "160")
public class TheBlackGate extends Card {

    public TheBlackGate() {
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(3));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new MakeTargetCreatureCantBeBlockedByMostLifePlayerEffect()),
                "{1}{B}, {T}: Choose a player with the most life or tied for most life. Target creature can't be blocked by creatures that player controls this turn.",
                TargetFilters.creature()));
    }
}
