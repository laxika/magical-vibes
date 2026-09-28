package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.CreatureSubtypeDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "2")
@CardRegistration(set = "MOC", collectorNumber = "89")
@CardRegistration(set = "MOC", collectorNumber = "135")
public class BrimazBlightOfOreskos extends Card {

    public BrimazBlightOfOreskos() {
        // Whenever you cast a Phyrexian creature or artifact creature spell, incubate X,
        // where X is that spell's mana value.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.PHYREXIAN))),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE)))
                )),
                List.of(new CreateXTokenWithXCountersEffect(
                        incubatorToken(), new EventValue(), CounterType.PLUS_ONE_PLUS_ONE))
        ));

        // At the beginning of each end step, if a Phyrexian died under your control this turn,
        // proliferate.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new CreatureSubtypeDiedUnderYourControlThisTurn(CardSubtype.PHYREXIAN),
                new ProliferateEffect()));
    }

    private static CreateTokenEffect incubatorToken() {
        ActivatedAbility transform = new ActivatedAbility(
                false,
                "{2}",
                List.of(new TransformSelfEffect()),
                "{2}: Transform this token."
        );
        return new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Incubator", 0, 0, null, null,
                List.of(), Set.of(), Set.of(), false, false, Map.of(), List.of(transform),
                false, false, false, 0, Set.of()
        );
    }
}
