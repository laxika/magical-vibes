package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SourcePermanentControllerLosesLifeEffect;

import java.util.List;

@CardRegistration(set = "SLC", collectorNumber = "24")
@CardRegistration(set = "SLC", collectorNumber = "51")
public class XantchaSleeperAgent extends Card {

    public XantchaSleeperAgent() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentGainsControlOfSourceEffect());
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.STATIC, new CantAttackCardOwnerEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new SourcePermanentControllerLosesLifeEffect(2), new DrawCardEffect(1)),
                "{3}: Xantcha's controller loses 2 life and you draw a card. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
