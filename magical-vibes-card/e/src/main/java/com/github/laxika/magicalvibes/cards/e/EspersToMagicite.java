package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect;

@CardRegistration(set = "FIC", collectorNumber = "43")
@CardRegistration(set = "FIC", collectorNumber = "114")
public class EspersToMagicite extends Card {

    public EspersToMagicite() {
        addEffect(EffectSlot.SPELL, new ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect());
    }
}
