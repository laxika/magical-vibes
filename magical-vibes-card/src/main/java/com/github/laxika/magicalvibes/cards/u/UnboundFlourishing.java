package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleXValueForPermanentSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "189")
@CardRegistration(set = "SOC", collectorNumber = "126")
public class UnboundFlourishing extends Card {

    public UnboundFlourishing() {
        addEffect(EffectSlot.STATIC, new DoubleXValueForPermanentSpellEffect());

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CopyControllerCastSpellOnSpellCastEffect(
                new CardAllOfPredicate(List.of(
                        new CardHasXInManaCostPredicate(),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))),
                null, null));

        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect(null, null, false, false, null, true));
    }
}
