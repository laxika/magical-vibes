package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;

public class HurkylsRecall extends Card {

    public HurkylsRecall() {
        super("Hurkyl's Recall", CardType.INSTANT, "{1}{U}", CardColor.BLUE);

        setCardText("Return all artifacts target player owns to their hand.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ReturnArtifactsTargetPlayerOwnsToHandEffect());
    }
}
