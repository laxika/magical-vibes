package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseCastCostForChosenNameSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesChosenNameWithSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "15")
@CardRegistration(set = "DBL", collectorNumber = "15")
public class CurseOfSilence extends Card {

    public CurseOfSilence() {
        addStaticEffect(new ChooseCardNameOnEnterEffect());
        addStaticEffect(new IncreaseCastCostForChosenNameSpellsEffect(2));
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntrySharesChosenNameWithSourcePredicate(),
                                new StackEntryControlledByEnchantedPlayerPredicate()))),
                new MayEffect(new SacrificeSelfThenEffect(new DrawCardEffect()),
                        "Sacrifice Curse of Silence to draw a card?"));
    }
}
