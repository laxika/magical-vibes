package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.ManaConfluence;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "325")
@CardRegistration(set = "MB2", collectorNumber = "561")
public class DwarvenConfluencer extends Card {

    public DwarvenConfluencer() {
        PermanentPredicateTargetFilter nontokenLand = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                "Target must be a nontoken land");

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentThenEffect(
                        new CreateTokenCopyOfCardEffect(
                                new ManaConfluence(), new CreateTokenCopyOfTargetPermanentEffect()),
                        ThenEffectRecipient.TARGET_CONTROLLER)),
                "{T}: Destroy target nontoken land. Its controller creates a Mana Confluence token. "
                        + "(It's a land with \"{T}, Pay 1 life: Add one mana of any color.\")",
                nontokenLand));
    }
}
