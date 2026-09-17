package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "37")
public class Silkguard extends Card {

    public Silkguard() {
        targetX(TargetFilters.creatureYouControl(), 100)
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE));

        PermanentPredicate modifiedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsModifiedPredicate()));
        PermanentPredicate hexproofPermanent = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.AURA),
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                modifiedCreature));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS, hexproofPermanent));
    }
}
