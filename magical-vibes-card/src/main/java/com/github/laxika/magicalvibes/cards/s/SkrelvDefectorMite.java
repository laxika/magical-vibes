package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "33")
public class SkrelvDefectorMite extends Card {

    public SkrelvDefectorMite() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(true, "{W/P}", List.of(
                new GrantKeywordEffect(Keyword.TOXIC, GrantScope.TARGET, new PermanentIsCreaturePredicate()),
                new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)),
                new GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect()
        ), "{W/P}, {T}: Choose a color. Another target creature you control gains toxic 1 and "
                + "hexproof from that color until end of turn. It can't be blocked by creatures of "
                + "that color this turn.", new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature you control")));
    }
}
