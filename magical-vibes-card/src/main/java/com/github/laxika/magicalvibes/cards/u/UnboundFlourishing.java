package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleXValueOfTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1331")
public class UnboundFlourishing extends Card {

    public UnboundFlourishing() {
        CardPredicate permanentSpellWithX = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardHasXInManaCostPredicate()));
        CardPredicate instantOrSorceryWithX = new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                new CardHasXInManaCostPredicate()));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                permanentSpellWithX,
                List.of(new DoubleXValueOfTriggeringSpellEffect())));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopyControllerCastSpellOnSpellCastEffect(instantOrSorceryWithX, null, null));
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                CopyControllerActivatedAbilityTriggerEffect.withXInActivationCost());
    }
}
