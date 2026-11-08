package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForAnotherOpponentPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCouldTargetAnotherPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "72")
@CardRegistration(set = "M3C", collectorNumber = "124")
public class ExterminatorMagmarch extends Card {

    public ExterminatorMagmarch() {
        PermanentPredicate nonlandPermanentAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        // The intervening "if" ("if another opponent controls one or more nonland permanents that spell could target")
        // is the last stack-entry filter; the copy effect makes no copy when no such permanent is left as it resolves.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                                new StackEntryTargetsPermanentPredicate(nonlandPermanentAnOpponentControls),
                                new StackEntryCouldTargetAnotherPermanentPredicate(
                                        nonlandPermanentAnOpponentControls, true)))),
                new CopySpellForAnotherOpponentPermanentEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Exterminator Magmarch."
        ));
    }
}
