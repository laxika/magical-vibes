package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "127")
public class FlurryOfWings extends Card {

    public FlurryOfWings() {
        // Create X 1/1 white Bird Soldier creature tokens with flying, where X is the number of attacking creatures.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER),
                "Bird Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.BIRD, CardSubtype.SOLDIER),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
