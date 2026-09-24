package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "454")
public class BreyaEtheriumShaper extends Card {

    public BreyaEtheriumShaper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Thopter", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(sacrificeTwoArtifacts(), new DealDamageToTargetPlayerOrPlaneswalkerEffect(3)),
                "{2}, Sacrifice two artifacts: Breya deals 3 damage to target player or planeswalker."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(sacrificeTwoArtifacts(), new BoostTargetCreatureEffect(-4, -4)),
                "{2}, Sacrifice two artifacts: Target creature gets -4/-4 until end of turn.",
                TargetFilters.creature()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(sacrificeTwoArtifacts(), new GainLifeEffect(5)),
                "{2}, Sacrifice two artifacts: You gain 5 life."));
    }

    private static SacrificeMultiplePermanentsCost sacrificeTwoArtifacts() {
        return new SacrificeMultiplePermanentsCost(2, new PermanentIsArtifactPredicate());
    }
}
