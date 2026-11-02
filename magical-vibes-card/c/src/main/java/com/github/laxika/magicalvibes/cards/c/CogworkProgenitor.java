package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YLCI", collectorNumber = "1")
public class CogworkProgenitor extends Card {

    public CogworkProgenitor() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayEffect(
                        new ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect(
                                1, 1, CardSubtype.GNOME),
                        "Exile another artifact you control or an artifact card from your graveyard?"));
    }
}
