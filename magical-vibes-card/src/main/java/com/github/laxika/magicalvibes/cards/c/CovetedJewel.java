package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "799")
public class CovetedJewel extends Card {

    public CovetedJewel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardForTargetPlayerEffect(3));
        addEffect(EffectSlot.ON_OPPONENT_CREATURES_ATTACK_YOU_UNBLOCKED,
                new DrawCardForTargetPlayerEffect(3));
        addEffect(EffectSlot.ON_OPPONENT_CREATURES_ATTACK_YOU_UNBLOCKED,
                TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer());
        addEffect(EffectSlot.ON_OPPONENT_CREATURES_ATTACK_YOU_UNBLOCKED,
                new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(3)),
                "{T}: Add three mana of any one color."
        ));
    }
}
