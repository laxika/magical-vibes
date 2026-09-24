package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "FCA", collectorNumber = "21")
@CardRegistration(set = "SOA", collectorNumber = "1")
public class AkromasWill extends Card {

    public AkromasWill() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption("Creatures you control gain flying, vigilance, and double strike until end of turn.",
                        new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.DOUBLE_STRIKE),
                                GrantScope.OWN_CREATURES)),
                new ChooseOneEffect.ChooseOneOption("Creatures you control gain lifelink, indestructible, and protection from each color until end of turn.",
                        List.of(
                                new GrantKeywordEffect(Set.of(Keyword.LIFELINK, Keyword.INDESTRUCTIBLE), GrantScope.OWN_CREATURES),
                                new GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect(
                                        new ProtectionFromColorsEffect(EnumSet.allOf(CardColor.class)))))
        ), new ControlledCommanderAsCast()));
    }
}
