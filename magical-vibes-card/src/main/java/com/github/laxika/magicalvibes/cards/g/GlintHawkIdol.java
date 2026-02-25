package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "156")
public class GlintHawkIdol extends Card {

    public GlintHawkIdol() {
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new MayEffect(
                new AnimateSelfWithStatsEffect(2, 2, List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING)),
                "Have Glint Hawk Idol become a 2/2 Bird artifact creature with flying?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new AnimateSelfWithStatsEffect(2, 2, List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING))),
                "{W}: Glint Hawk Idol becomes a 2/2 Bird artifact creature with flying until end of turn."
        ));
    }
}
