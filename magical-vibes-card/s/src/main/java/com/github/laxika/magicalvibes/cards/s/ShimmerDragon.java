package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "236")
@CardRegistration(set = "MKC", collectorNumber = "117")
@CardRegistration(set = "BRC", collectorNumber = "95")
@CardRegistration(set = "ELD", collectorNumber = "317")
@CardRegistration(set = "FDC", collectorNumber = "75")
public class ShimmerDragon extends Card {

    public ShimmerDragon() {
        // As long as you control four or more artifacts, this creature has hexproof.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(4, new PermanentIsArtifactPredicate()),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));

        // Tap two untapped artifacts you control: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new DrawCardEffect(1)
                ),
                "Tap two untapped artifacts you control: Draw a card."
        ));
    }
}
