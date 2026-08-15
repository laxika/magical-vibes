package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "74")
public class KingMacarTheGoldCursed extends Card {

    public KingMacarTheGoldCursed() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new MayEffect(new ExileTargetPermanentThenEffect(
                        CreateTokenEffect.ofArtifactToken(1, "Gold", List.of(), List.of(new ActivatedAbility(
                                false, null,
                                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                                "Sacrifice this token: Add one mana of any color."
                        ))), ThenEffectRecipient.CONTROLLER), "Exile target creature?"));
    }
}
