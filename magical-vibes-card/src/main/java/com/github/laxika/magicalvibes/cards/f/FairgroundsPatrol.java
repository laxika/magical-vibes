package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "13")
public class FairgroundsPatrol extends Card {

    public FairgroundsPatrol() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect("Thopter", 1, 1, null,
                                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                                Set.of(CardType.ARTIFACT))
                ),
                "{1}{W}, Exile this card from your graveyard: Create a 1/1 colorless Thopter artifact creature token with flying. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
