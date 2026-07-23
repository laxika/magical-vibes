package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ChosenColorStrictlyMostCommonAmongOpponentNontokens;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "10")
public class CallToArms extends Card {

    public CallToArms() {
        // "As this enchantment enters, choose a color and an opponent."
        // Opponent is implicit (single-opponent model, like Cursed Rack / Nyxathid).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());

        // "White creatures get +1/+1 as long as the chosen color is the most common color among
        // nontoken permanents the chosen player controls but isn't tied for most common."
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ChosenColorStrictlyMostCommonAmongOpponentNontokens(),
                new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)))));

        // "When the chosen color isn't the most common … or is tied for most common, sacrifice this."
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> {
                    if (sourcePermanent.getChosenColor() == null) {
                        return false;
                    }
                    return !ChosenColorStrictlyMostCommonAmongOpponentNontokens.isStrictlyMostCommon(
                            gameData, sourcePermanent, controllerId);
                },
                List.of(new SacrificeSelfEffect()),
                "Call to Arms's state-triggered ability"
        ));
    }
}
