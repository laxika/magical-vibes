package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "8")
public class EightAndAHalfTails extends Card {

    public EightAndAHalfTails() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantProtectionFromColorUntilEndOfTurnEffect(
                        CardColor.WHITE,
                        new PermanentControlledBySourceControllerPredicate(),
                        GrantScope.TARGET,
                        TargetPredicates.permanent())),
                "{1}{W}: Target permanent you control gains protection from white until end of turn.",
                TargetFilters.permanentYouControl()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new GrantColorUntilEndOfTurnEffect(CardColor.WHITE, false, true)),
                "{1}: Target spell or permanent becomes white until end of turn."
        ));
    }
}
