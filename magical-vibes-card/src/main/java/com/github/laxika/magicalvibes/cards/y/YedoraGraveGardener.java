package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect;

@CardRegistration(set = "MUL", collectorNumber = "30")
@CardRegistration(set = "MUL", collectorNumber = "95")
@CardRegistration(set = "MUL", collectorNumber = "160")
public class YedoraGraveGardener extends Card {

    public YedoraGraveGardener() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new MayEffect(new ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect(CardSubtype.FOREST),
                        "Return it to the battlefield face down as a Forest?"));
    }
}
