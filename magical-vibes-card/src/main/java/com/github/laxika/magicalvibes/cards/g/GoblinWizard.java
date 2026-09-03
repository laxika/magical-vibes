package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "69")
public class GoblinWizard extends Card {

    public GoblinWizard() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardSubtypePredicate(CardSubtype.GOBLIN))),
                                "Goblin permanent"),
                        "Put a Goblin permanent card from your hand onto the battlefield?"
                )),
                "{T}: You may put a Goblin permanent card from your hand onto the battlefield."
        ));

        PermanentPredicate goblin = new PermanentHasSubtypePredicate(CardSubtype.GOBLIN);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new GrantProtectionFromColorUntilEndOfTurnEffect(
                        CardColor.WHITE, goblin, GrantScope.TARGET, TargetPredicates.permanent())),
                "{R}: Target Goblin gains protection from white until end of turn.",
                new PermanentPredicateTargetFilter(goblin, "Target must be a Goblin")
        ));
    }
}
