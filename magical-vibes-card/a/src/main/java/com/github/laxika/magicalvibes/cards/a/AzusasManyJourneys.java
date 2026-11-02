package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LikenessOfTheSeeker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "172")
public class AzusasManyJourneys extends Card {

    public AzusasManyJourneys() {
        setBackFaceCard(new LikenessOfTheSeeker());

        addTrigger(Triggers.chapter(1), new PlayAdditionalLandsEffect(1));
        addTrigger(Triggers.chapter(2), new GainLifeEffect(3));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "LikenessOfTheSeeker";
    }
}
