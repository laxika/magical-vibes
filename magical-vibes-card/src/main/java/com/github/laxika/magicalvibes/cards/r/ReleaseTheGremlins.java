package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "96")
public class ReleaseTheGremlins extends Card {

    public ReleaseTheGremlins() {
        targetX(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Targets must be artifacts"
        ), 100).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(new XValue(), "Gremlin", 2, 2,
                CardColor.RED, List.of(CardSubtype.GREMLIN), Set.of(), Set.of()));
    }
}
