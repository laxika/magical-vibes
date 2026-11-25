package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "13")
public class AGoldenOpportunity extends Card {

    public AGoldenOpportunity() {
        addTrigger(Triggers.chapter(1),
                new ConjureCardToBattlefieldEffect("ELD", "160"));
        addTrigger(Triggers.chapter(2), chapterTwoOrThreeEffect());
        addTrigger(Triggers.chapter(3), chapterTwoOrThreeEffect());
    }

    private static UnlessPaysEffect chapterTwoOrThreeEffect() {
        return UnlessPaysEffect.youMayPay(AllOfCost.of(
                        new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.BIRD)),
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "artifact", false)),
                new ConjureCardToBattlefieldEffect("ELD", "220"))
                .withPrompt("Tap an untapped Bird you control and sacrifice an artifact?");
    }
}
