package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "63")
public class ShipbreakerKraken extends Card {

    public ShipbreakerKraken() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{U}{U}",
                List.of(new MonstrosityEffect(4)),
                "{6}{U}{U}: Monstrosity 4."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));

        target(TargetFilters.creature(), 0, 4)
                .addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS, DoesntUntapEffect.targetWhileSourceOnBattlefield());
    }
}
