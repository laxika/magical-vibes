package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "14")
@CardRegistration(set = "OTC", collectorNumber = "50")
public class ForgersFoundry extends Card {

    public ForgersFoundry() {
        CardPredicate instantOrSorceryManaValueThreeOrLess = new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                new CardMaxManaValuePredicate(3)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE)
                        .withProducingSourceForSpellCastTriggers()),
                "{T}: Add {U}. When you spend this mana to cast an instant or sorcery spell with mana value 3 or less, you may exile that spell instead of putting it into its owner's graveyard as it resolves."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                SpellCastTriggerEffect.usingManaProducedBySource(
                        instantOrSorceryManaValueThreeOrLess,
                        List.of(new ExileTriggeringSpellEffect())),
                "Exile that spell instead of putting it into its owner's graveyard as it resolves?"
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}{U}",
                List.of(new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(
                        null, true)),
                "{3}{U}{U}, {T}: You may cast any number of spells from among cards exiled with this artifact without paying their mana costs. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
