package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "11")
public class FinalShowdown extends Card {

    public FinalShowdown() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{1}", "{1}", "{3}{W}{W}")));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "All creatures lose all abilities until end of turn",
                        new LosesAllAbilitiesEffect(GrantScope.ALL_CREATURES, EffectDuration.UNTIL_END_OF_TURN)),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose a creature you control. It gains indestructible until end of turn",
                        new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword.INDESTRUCTIBLE, null)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy all creatures",
                        new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()))
        )));
    }
}
