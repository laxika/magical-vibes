package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "12")
@CardRegistration(set = "OTC", collectorNumber = "48")
public class WeRideAtDawn extends Card {

    public WeRideAtDawn() {
        addStaticEffect(new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.CONVOKE,
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSupertypePredicate(CardSupertype.LEGENDARY)))));

        CreateTokenEffect mercenaryToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Mercenary", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.MERCENARY), Set.of(), Set.of(), false, false, List.of(),
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

        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentIsCommanderPredicate(true), mercenaryToken));
    }
}
