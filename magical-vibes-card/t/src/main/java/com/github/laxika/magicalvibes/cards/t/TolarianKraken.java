package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "80")
public class TolarianKraken extends Card {

    public TolarianKraken() {
        addTrigger(Triggers.youDrawCard(), UnlessPaysEffect.youMayPay("{1}", new QueueReflexiveAbilityEffect(new MayEffect(
                        new TapOrUntapTargetPermanentEffect(new PermanentIsCreaturePredicate()),
                        "Tap or untap target creature?"))).withPrompt("Pay {1} to use its ability?"));
    }
}
