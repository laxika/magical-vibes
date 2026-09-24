package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerIsStartingPlayer;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "63")
public class ForsakenCrossroads extends Card {

    public ForsakenCrossroads() {
        // Forsaken Crossroads enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // As Forsaken Crossroads enters the battlefield, choose a color.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());

        // When Forsaken Crossroads enters the battlefield, scry 1. If you weren't the starting
        // player, you may untap Forsaken Crossroads instead.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ScryEffect(1),
                ConditionalEffect.unless(
                        new NotCondition(new ControllerIsStartingPlayer()),
                        new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF),
                                "Untap Forsaken Crossroads instead?"))));

        // {T}: Add one mana of the chosen color.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."));
    }
}
