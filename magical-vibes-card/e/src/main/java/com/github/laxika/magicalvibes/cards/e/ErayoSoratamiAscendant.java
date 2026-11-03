package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "35")
public class ErayoSoratamiAscendant extends Card {

    public ErayoSoratamiAscendant() {
        setBackFaceCard(new ErayosEssence());

        // Flipping is not transforming (CR 701.27a, CR 710), so the flip uses the flip form of
        // TransformToBackFaceEffect: no transform triggers fire and transform prevention does not apply.
        addTrigger(Triggers.playerCastsSpell().nthThisTurn(4).countingEveryPlayersSpells(),
                new TransformToBackFaceEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "ErayosEssence";
    }
}
