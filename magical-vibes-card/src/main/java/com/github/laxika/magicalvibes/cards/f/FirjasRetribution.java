package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "210")
public class FirjasRetribution extends Card {

    private static final PermanentPredicate ANGEL = new PermanentHasSubtypePredicate(CardSubtype.ANGEL);
    private static final PermanentPredicate SMALLER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentPowerLessThanSourcePowerPredicate()
    ));

    public FirjasRetribution() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                "Angel Warrior", 4, 4, CardColor.WHITE,
                List.of(CardSubtype.ANGEL, CardSubtype.WARRIOR),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new DestroyTargetPermanentEffect()),
                        "{T}: Destroy target creature with power less than this creature's power.",
                        new PermanentPredicateTargetFilter(
                                SMALLER_CREATURE,
                                "Target must be a creature with power less than this creature's power"
                        )
                ),
                GrantScope.OWN_CREATURES,
                ANGEL,
                EffectDuration.UNTIL_END_OF_TURN
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE,
                GrantScope.OWN_CREATURES,
                ANGEL
        ));
    }
}
