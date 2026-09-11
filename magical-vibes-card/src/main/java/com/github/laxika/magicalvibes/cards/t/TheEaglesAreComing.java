package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentsThenEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "12")
public class TheEaglesAreComing extends Card {

    public TheEaglesAreComing() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{W}{W}"));

        OwnedPermanentPredicateTargetFilter creatureYouOwn = new OwnedPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                "Target must be a creature you own");
        targetWhenKicked(creatureYouOwn, 1, 1, 0, 99)
                .addEffect(EffectSlot.SPELL, new ReturnTargetPermanentsThenEffect(
                        RegisterDelayedCreateTokenAtNextUpkeepEffect.atAnyPlayerNextUpkeep(new CreateTokenEffect(
                                new EventValue(), "Bird Soldier", 4, 4, CardColor.WHITE,
                                List.of(CardSubtype.BIRD, CardSubtype.SOLDIER),
                                Set.of(Keyword.FLYING), Set.of()))));
    }
}
