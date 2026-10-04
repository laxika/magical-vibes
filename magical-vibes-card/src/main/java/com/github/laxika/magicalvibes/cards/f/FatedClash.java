package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CreatureAttackingAndCreatureBlocking;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "449")
public class FatedClash extends Card {

    public FatedClash() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{3}{W}{W}")),
                new CreatureAttackingAndCreatureBlocking(),
                true));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
