package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "75")
public class SmokeBomb extends Card {

    public SmokeBomb() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ALL_CREATURES));

        var ownCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificePermanentThenEffect(
                new PermanentIsSourcePermanentPredicate(),
                new MakeCreatureUnblockableEffect(ownCreature),
                "this artifact"));
    }
}
