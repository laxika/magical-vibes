package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellCopiedThisWay;
import com.github.laxika.magicalvibes.model.condition.TriggeringSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsAnyPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "7")
public class ShikoAndNarsetUnified extends Card {

    public ShikoAndNarsetUnified() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new ConditionalEffect(new TriggeringSpellMatches(new StackEntryAnyOfPredicate(List.of(
                        new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate()),
                        new StackEntryTargetsAnyPlayerPredicate()))),
                        new CopyTriggeringSpellEffect()),
                new ConditionalEffect(new NotCondition(new SpellCopiedThisWay()), new DrawCardEffect()));
    }
}
