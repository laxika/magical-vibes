package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;

@CardRegistration(set = "ATQ", collectorNumber = "76")
public class UrzasMiter extends Card {

    public UrzasMiter() {
        addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringArtifactControllerConditionalEffect(
                        new MayPayManaEffect("{3}", new DrawCardEffect(), "Pay {3} to draw a card?"), true));
    }
}
