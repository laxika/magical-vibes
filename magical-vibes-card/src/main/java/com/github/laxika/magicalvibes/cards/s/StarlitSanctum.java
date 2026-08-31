package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "325")
public class StarlitSanctum extends Card {

    public StarlitSanctum() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        clericSacrificeCost(false, true),
                        new GainLifeEffect(new XValue())
                ),
                "{W}, {T}, Sacrifice a Cleric creature: You gain life equal to the sacrificed creature's toughness."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        clericSacrificeCost(true, false),
                        new LoseLifeEffect(new XValue(), LoseLifeRecipient.TARGET_PLAYER)
                ),
                "{B}, {T}, Sacrifice a Cleric creature: Target player loses life equal to the sacrificed creature's power."
        ));
    }

    private SacrificePermanentCost clericSacrificeCost(boolean trackPower, boolean trackToughness) {
        return new SacrificePermanentCost(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.CLERIC)
                )),
                "Sacrifice a Cleric creature",
                false,
                trackPower,
                false,
                trackToughness
        );
    }
}
