package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.ControllerCastThreeOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "3")
public class StellaLeeWildCard extends Card {

    public StellaLeeWildCard() {
        var controlledInstantOrSorcery = new StackEntryAllOfPredicate(List.of(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.SORCERY_SPELL)),
                new StackEntryControlledByPredicate()));

        // Whenever you cast your second spell each turn, exile the top card of your library. Until
        // the end of your next turn, you may play that card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(new ExileTopCardsMayPlayUntilNextTurnEffect(1))));

        // {T}: Copy target instant or sorcery spell you control. You may choose new targets for the
        // copy. Activate only if you've cast three or more spells this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CopySpellEffect()),
                "{T}: Copy target instant or sorcery spell you control. You may choose new targets for the copy. Activate only if you've cast three or more spells this turn.",
                new StackEntryPredicateTargetFilter(
                        controlledInstantOrSorcery,
                        "Target must be an instant or sorcery spell you control."))
                .withActivationCondition(
                        new ControllerCastThreeOrMoreSpellsThisTurn(new CardTruePredicate()),
                        "Activate only if you've cast three or more spells this turn"));
    }
}
