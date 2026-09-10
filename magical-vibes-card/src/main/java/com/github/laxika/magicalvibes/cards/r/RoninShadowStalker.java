package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "112")
public class RoninShadowStalker extends Card {

    public RoninShadowStalker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayLifeCost(2),
                        new AwardRestrictedManaOfColorsEffect(
                                ManaColor.COLORS,
                                new Fixed(2),
                                new ManaRestriction.SubtypeSpellsOrAbilities(CardSubtype.EQUIPMENT),
                                true)
                ),
                "Pay 2 life: Add two mana of any one color. Spend this mana only to cast Equipment spells or activate equip abilities.",
                1
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                                new PermanentAttachedToSourcePermanentPredicate()
                        )), "Sacrifice an Equipment attached to Ronin", false),
                        new BoostTargetCreatureEffect(-4, -4)
                ),
                "{T}, Sacrifice an Equipment attached to Ronin: Target creature gets -4/-4 until end of turn. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
