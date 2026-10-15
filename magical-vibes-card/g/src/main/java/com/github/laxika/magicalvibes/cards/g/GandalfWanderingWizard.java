package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "41")
public class GandalfWanderingWizard extends Card {

    public GandalfWanderingWizard() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(3));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new ShuffleSelfIntoOwnerLibraryEffect(
                        new DrawCardEffect(3), ThenEffectRecipient.TARGET_OWNER)),
                "{6}: Gandalf's owner shuffles him into their library and draws three cards."
        ));
    }
}
