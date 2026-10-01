package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerIsStartingPlayer;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToTwoColorsFromHandOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "30")
public class DesertCenote extends Card {

    public DesertCenote() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControllerIsStartingPlayer(), new EntersTappedEffect()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseUpToTwoColorsFromHandOnEnterEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(AwardAnyColorManaEffect.fromSourceChosenColors()),
                "{T}: Add one mana of a chosen color."));
    }
}
