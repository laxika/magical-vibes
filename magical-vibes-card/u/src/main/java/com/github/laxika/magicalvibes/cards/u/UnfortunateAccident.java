package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "111")
public class UnfortunateAccident extends Card {

    public UnfortunateAccident() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{2}{B}", "{1}")));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 red Mercenary creature token with \"{T}: Target creature you control gets +1/+0 until end of turn. Activate only as a sorcery.\"",
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Mercenary", 1, 1, CardColor.RED, null,
                                List.of(CardSubtype.MERCENARY), Set.of(), Set.of(), false, false, Map.of(),
                                List.of(new ActivatedAbility(
                                        true,
                                        null,
                                        List.of(new BoostTargetCreatureEffect(1, 0)),
                                        "{T}: Target creature you control gets +1/+0 until end of turn. Activate only as a sorcery.",
                                        TargetFilters.creatureYouControl(),
                                        null,
                                        null,
                                        ActivationTimingRestriction.SORCERY_SPEED
                                )),
                                false, false, false, 0, Set.of())
                )
        )));
    }
}
