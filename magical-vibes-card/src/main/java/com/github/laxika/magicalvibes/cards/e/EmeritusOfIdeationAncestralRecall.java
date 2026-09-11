package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AncestralRecall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

/**
 * Emeritus of Ideation // Ancestral Recall (SOS 45).
 */
@CardRegistration(set = "SOS", collectorNumber = "45")
public class EmeritusOfIdeationAncestralRecall extends Card {

    public EmeritusOfIdeationAncestralRecall() {
        setBackFaceCard(new AncestralRecall());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.unless(
                new GraveyardCardThreshold(8, null),
                new MayEffect(
                        SequenceEffect.of(
                                new ExileGraveyardCardsEffect(8, GraveyardExileScope.OWN),
                                new BecomePreparedEffect()),
                        "Exile eight cards from your graveyard to prepare this creature?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "AncestralRecall";
    }
}
