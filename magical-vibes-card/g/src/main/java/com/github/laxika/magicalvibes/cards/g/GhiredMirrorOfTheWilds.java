package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "205")
public class GhiredMirrorOfTheWilds extends Card {

    public GhiredMirrorOfTheWilds() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new CreateTokenCopyOfTargetPermanentEffect()),
                        "{T}: Create a token that's a copy of target token you control that entered the battlefield this turn.",
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsTokenPredicate(),
                                        new PermanentEnteredBattlefieldThisTurnPredicate())),
                                "Target must be a token you control that entered the battlefield this turn")
                ),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        ));
    }
}
