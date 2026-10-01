package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "587")
public class LakeSilencio extends Card {

    public LakeSilencio() {
        addEffect(EffectSlot.STATIC, GrantSpellCastingAbilityToSpellsEffect.allPlayers(
                Keyword.SPLIT_SECOND, new CardTruePredicate()));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                        new MarkTargetCreatureExileInsteadOfDieThisTurnEffect(),
                        new DealDamageToTargetCreatureEffect(6)));
    }
}
