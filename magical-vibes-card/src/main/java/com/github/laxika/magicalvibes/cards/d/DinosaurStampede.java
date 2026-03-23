package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "XLN", collectorNumber = "140")
public class DinosaurStampede extends Card {

    public DinosaurStampede() {
        // Attacking creatures get +2/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(2, 0, new PermanentIsAttackingPredicate()));
        // Dinosaurs you control gain trample until end of turn.
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));
    }
}
