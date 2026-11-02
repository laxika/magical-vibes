package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "160")
@CardRegistration(set = "KHM", collectorNumber = "298")
public class BattleMammoth extends Card {

    public BattleMammoth() {
        addTrigger(Triggers.permanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
        addCastingOption(new ForetellCast("{2}{G}{G}"));
    }
}
