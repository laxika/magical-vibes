package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "255")
public class DollhouseOfHorrors extends Card {

    public DollhouseOfHorrors() {
        PermanentCount constructs = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CONSTRUCT), CountScope.CONTROLLER);
        CreateTokenCopyOfImprintedCardEffect tokenCopy = new CreateTokenCopyOfImprintedCardEffect(
                false,
                false,
                List.of(CardSubtype.CONSTRUCT),
                Set.of(CardType.ARTIFACT),
                0,
                0,
                true,
                List.of(new DynamicStaticBoostEffect(constructs, constructs, GrantScope.SELF)),
                false);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE, false, true), tokenCopy),
                "{1}, {T}, Exile a creature card from your graveyard: Create a token that's a copy of the exiled card, except it's a 0/0 Construct artifact in addition to its other types and it has \"This token gets +1/+1 for each Construct you control.\" It gains haste until end of turn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
