package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "157")
public class PiaAndKiranNalaar extends Card {

    public PiaAndKiranNalaar() {
        // When this creature enters, create two 1/1 colorless Thopter artifact creature tokens with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)));

        // {2}{R}, Sacrifice an artifact: Pia and Kiran Nalaar deals 2 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new DealDamageToAnyTargetEffect(2)),
                "{2}{R}, Sacrifice an artifact: Pia and Kiran Nalaar deals 2 damage to any target."
        ));
    }
}
