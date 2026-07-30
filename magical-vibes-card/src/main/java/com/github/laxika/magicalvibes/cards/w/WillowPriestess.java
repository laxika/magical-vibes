package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "100")
public class WillowPriestess extends Card {

    public WillowPriestess() {
        // {T}: You may put a Faerie permanent card from your hand onto the battlefield.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardSubtypePredicate(CardSubtype.FAERIE))),
                                "Faerie permanent"),
                        "Put a Faerie permanent card from your hand onto the battlefield?"
                )),
                "{T}: You may put a Faerie permanent card from your hand onto the battlefield."
        ));

        // {2}{G}: Target green creature gains protection from black until end of turn.
        PermanentPredicate greenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.BLACK, greenCreature)),
                "{2}{G}: Target green creature gains protection from black until end of turn.",
                new PermanentPredicateTargetFilter(greenCreature, "Target must be a green creature")
        ));
    }
}
