package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromCardTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "18")
public class DevotedCaretaker extends Card {

    public DevotedCaretaker() {
        PermanentPredicate permanentYouControl = new PermanentControlledBySourceControllerPredicate();
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new GrantProtectionFromCardTypeUntilEndOfTurnEffect(
                                CardType.INSTANT, permanentYouControl, TargetPredicates.permanent()),
                        new GrantProtectionFromCardTypeUntilEndOfTurnEffect(
                                CardType.SORCERY, permanentYouControl, TargetPredicates.permanent())
                ),
                "{W}, {T}: Target permanent you control gains protection from instant spells and from sorcery spells until end of turn.",
                TargetFilters.permanentYouControl()
        ));
    }
}
