package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "205")
public class WolfwillowHaven extends Card {

    public WolfwillowHaven() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                        new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(ManaColor.GREEN)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.WOLF), Set.of(), Set.of())
                ),
                "{4}{G}, Sacrifice this Aura: Create a 2/2 green Wolf creature token. Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
