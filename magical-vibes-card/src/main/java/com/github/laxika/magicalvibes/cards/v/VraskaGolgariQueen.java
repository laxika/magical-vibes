package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "213")
public class VraskaGolgariQueen extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever a creature you control deals combat damage to a player, that player loses the game.";

    public VraskaGolgariQueen() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new MayEffect(
                        new SacrificePermanentThenEffect(
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                                SequenceEffect.of(new GainLifeEffect(1), new DrawCardEffect(1)),
                                "another permanent"),
                        "Sacrifice another permanent?")),
                "+2: You may sacrifice another permanent. If you do, you gain 1 life and draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect()),
                "−3: Destroy target nonland permanent with mana value 3 or less.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentMaxManaValuePredicate(3))),
                        "Target must be a nonland permanent with mana value 3 or less"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new CreateEmblemEffect(
                        List.of(new GrantTriggeredAbilityEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new TargetPlayerLosesGameEffect(null),
                                GrantScope.ALL_OWN_CREATURES)),
                        EMBLEM_TEXT)),
                "−9: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
