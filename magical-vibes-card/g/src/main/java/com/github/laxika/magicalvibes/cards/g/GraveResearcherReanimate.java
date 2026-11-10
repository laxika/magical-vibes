package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.Reanimate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Grave Researcher // Reanimate (SOS 85). */
@CardRegistration(set = "SOS", collectorNumber = "85")
public class GraveResearcherReanimate extends Card {

    public GraveResearcherReanimate() {
        setBackFaceCard(new Reanimate());

        // At the beginning of your upkeep, surveil 1. Then if there are three or more creature
        // cards in your graveyard, this creature becomes prepared.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new SurveilEffect(1),
                new ConditionalEffect(
                        new GraveyardCardThreshold(3, new CardTypePredicate(CardType.CREATURE)),
                        new BecomePreparedEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "Reanimate";
    }
}
