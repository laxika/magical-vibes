package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "135")
public class LanternOfInsight extends Card {

    public LanternOfInsight() {
        // Players play with the top card of their libraries revealed.
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        // {T}, Sacrifice this artifact: Target player shuffles.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new ShuffleLibraryEffect(true)),
                "{T}, Sacrifice this artifact: Target player shuffles."
        ));
    }
}
