package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "587")
public class LakeSilencio extends Card {

    public LakeSilencio() {
        addStaticEffect(GrantSpellCastingAbilityToSpellsEffect.allPlayers(
                Keyword.SPLIT_SECOND, new CardTruePredicate()));

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                        new MarkTargetCreatureExileInsteadOfDieThisTurnEffect(),
                        new DealDamageToTargetCreatureEffect(6)));
    }
}
