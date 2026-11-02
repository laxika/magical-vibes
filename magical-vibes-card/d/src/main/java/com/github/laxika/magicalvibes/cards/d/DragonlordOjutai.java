package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "219")
@CardRegistration(set = "SLD", collectorNumber = "594")
@CardRegistration(set = "SLD", collectorNumber = "1973")
@CardRegistration(set = "PIO", collectorNumber = "219")
@CardRegistration(set = "SLC", collectorNumber = "2015")
@CardRegistration(set = "NCC", collectorNumber = "337")
public class DragonlordOjutai extends Card {

    public DragonlordOjutai() {
        addStaticEffect(new ConditionalEffect(
                new SourceUntapped(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
    }
}
