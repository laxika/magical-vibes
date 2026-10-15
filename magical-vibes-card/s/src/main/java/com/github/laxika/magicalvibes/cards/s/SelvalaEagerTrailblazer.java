package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfChosenColorEqualToDistinctCreaturePowersEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "231")
public class SelvalaEagerTrailblazer extends Card {

    public SelvalaEagerTrailblazer() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE), List.of(mercenaryToken())));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfChosenColorEqualToDistinctCreaturePowersEffect()),
                "{T}: Choose a color. Add one mana of that color for each different power among creatures you control."
        ));
    }

    private static CreateTokenEffect mercenaryToken() {
        return new CreateTokenEffect(
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
                false, false, false, 0, Set.of());
    }
}
