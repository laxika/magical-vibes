package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "140")
public class DragonbroodsRelic extends Card {

    public DragonbroodsRelic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                        new AwardAnyColorManaEffect()
                ),
                "{T}, Tap an untapped creature you control: Add one mana of any color."
        ));

        Map<EffectSlot, CardEffect> dragonTokenEffects =
                Map.of(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(3));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{U}{B}{R}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Reliquary Dragon",
                                4,
                                4,
                                null,
                                Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN),
                                List.of(CardSubtype.DRAGON),
                                Set.of(Keyword.FLYING, Keyword.LIFELINK),
                                Set.of(),
                                false,
                                false,
                                dragonTokenEffects,
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of()
                        )
                ),
                "{3}{W}{U}{B}{R}{G}, Sacrifice this artifact: Create a 4/4 Dragon creature token named "
                        + "Reliquary Dragon that's all colors. It has flying, lifelink, and \"When this token "
                        + "enters, it deals 3 damage to any target.\" Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
