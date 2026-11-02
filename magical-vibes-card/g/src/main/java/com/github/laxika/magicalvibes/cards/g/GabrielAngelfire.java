package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "76")
@CardRegistration(set = "LEG", collectorNumber = "226")
@CardRegistration(set = "ME3", collectorNumber = "148")
public class GabrielAngelfire extends Card {

    public GabrielAngelfire() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Flying", new GrantKeywordEffect(
                        Keyword.FLYING, GrantScope.SELF, GrantDuration.UNTIL_YOUR_NEXT_UPKEEP)),
                new ChooseOneEffect.ChooseOneOption("First strike", new GrantKeywordEffect(
                        Keyword.FIRST_STRIKE, GrantScope.SELF, GrantDuration.UNTIL_YOUR_NEXT_UPKEEP)),
                new ChooseOneEffect.ChooseOneOption("Trample", new GrantKeywordEffect(
                        Keyword.TRAMPLE, GrantScope.SELF, GrantDuration.UNTIL_YOUR_NEXT_UPKEEP)),
                new ChooseOneEffect.ChooseOneOption("Rampage 3", GrantEffectToTargetEffect.toSource(
                        new TriggeredAbility(Triggers.becomesBlocked(), rampageThree()),
                        EffectDuration.UNTIL_CONTROLLERS_NEXT_UPKEEP))
        )));
    }

    private static RampageEffect rampageThree() {
        return new RampageEffect(3);
    }
}
