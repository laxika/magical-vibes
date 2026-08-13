package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "73")
public class Gild extends Card {

    public Gild() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.ofArtifactToken(
                        1, "Gold", List.of(), List.of(new ActivatedAbility(
                                false, null,
                                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                                "Sacrifice this token: Add one mana of any color."
                        ))));
    }
}
