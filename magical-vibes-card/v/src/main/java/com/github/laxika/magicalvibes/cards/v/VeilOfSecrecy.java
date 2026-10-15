package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "59")
public class VeilOfSecrecy extends Card {

    public VeilOfSecrecy() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect());
        // Splice onto Arcane—Return a blue creature you control to its owner's hand. The splice cost
        // has no mana component, so only the return filter is supplied.
        addEffect(EffectSlot.STATIC, SpliceEffect.returning(CardSubtype.ARCANE, new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentColorInPredicate(Set.of(CardColor.BLUE))))));
    }
}
