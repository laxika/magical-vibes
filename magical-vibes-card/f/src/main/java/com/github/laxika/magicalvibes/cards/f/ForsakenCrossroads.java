package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerIsStartingPlayer;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "63")
@CardRegistration(set = "MB2", collectorNumber = "264")
public class ForsakenCrossroads extends Card {

    public ForsakenCrossroads() {
        // Forsaken Crossroads enters the battlefield tapped.
        addStaticEffect(new EntersTappedEffect());

        // As Forsaken Crossroads enters the battlefield, choose a color.
        addStaticEffect(new ChooseColorOnEnterEffect());

        // When Forsaken Crossroads enters the battlefield, scry 1. If you weren't the starting
        // player, you may untap Forsaken Crossroads instead.
        addTrigger(Triggers.enters(ObjectRelation.THIS), ConditionalEffect.unless(
                new ControllerIsStartingPlayer(), new ScryEffect(1)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), ConditionalEffect.unless(
                new NotCondition(new ControllerIsStartingPlayer()),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF),
                        "Untap Forsaken Crossroads instead?", new ScryEffect(1))));

        // {T}: Add one mana of the chosen color.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."));
    }
}
