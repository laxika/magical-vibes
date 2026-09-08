package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TombOfTheDuskRose;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "166")
public class ProfaneProcession extends Card {

    public ProfaneProcession() {
        setBackFaceCard(new TombOfTheDuskRose());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{B}",
                List.of(
                        new ExileTargetPermanentAndTrackWithSourceEffect(),
                        new ConditionalEffect(new SourceExiledCardsThreshold(3), new TransformSelfEffect())
                ),
                "{3}{W}{B}: Exile target creature. Then if there are three or more cards exiled with Profane Procession, transform it.",
                TargetFilters.creature()
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "TombOfTheDuskRose";
    }
}
