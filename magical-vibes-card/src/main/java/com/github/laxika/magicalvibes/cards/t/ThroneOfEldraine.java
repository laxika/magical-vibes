package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "WOC", collectorNumber = "28")
@CardRegistration(set = "WOC", collectorNumber = "40")
public class ThroneOfEldraine extends Card {

    public ThroneOfEldraine() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        ManaRestriction restriction = new ManaRestriction.MonocoloredSpells();
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardChosenColorManaEffect(restriction),
                        new AwardChosenColorManaEffect(restriction),
                        new AwardChosenColorManaEffect(restriction),
                        new AwardChosenColorManaEffect(restriction)),
                "{T}: Add four mana of the chosen color. Spend this mana only to cast monocolored spells of that color."));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DrawCardEffect(2)),
                "{3}, {T}: Draw two cards. Spend only mana of the chosen color to activate this ability.")
                .withSourceChosenColorManaOnly());
    }
}
