package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "99")
public class ViridianLorebearers extends Card {

    public ViridianLorebearers() {
        PermanentCount opponentArtifacts =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.OPPONENTS);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(new BoostTargetCreatureEffect(opponentArtifacts, opponentArtifacts)),
                "{3}{G}, {T}: Target creature gets +X/+X until end of turn, where X is the number of artifacts your opponents control."
        ));
    }
}
