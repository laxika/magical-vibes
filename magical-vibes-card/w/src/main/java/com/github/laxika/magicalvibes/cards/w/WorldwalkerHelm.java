package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddMapTokenToArtifactTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "7")
public class WorldwalkerHelm extends Card {

    public WorldwalkerHelm() {
        addEffect(EffectSlot.STATIC, new AddMapTokenToArtifactTokenCreationEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new CreateTokenCopyOfTargetPermanentEffect()),
                "{1}{U}, {T}: Create a token that's a copy of target artifact token you control.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsTokenPredicate())),
                        "Target must be an artifact token you control.")));
    }
}
