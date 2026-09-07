package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "231")
public class SistersOfStoneDeath extends Card {

    public SistersOfStoneDeath() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new MustBlockSourceEffect(null)),
                "{G}: Target creature blocks Sisters of Stone Death this turn if able."));

        var inCombatWithThis = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentInCombatWithSourcePredicate()
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                "{B}{G}: Exile target creature blocking or blocked by Sisters of Stone Death.",
                new PermanentPredicateTargetFilter(
                        inCombatWithThis,
                        "Target must be a creature blocking or blocked by Sisters of Stone Death")));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new ReturnCardExiledWithSourceToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), false, null)),
                "{2}{B}: Put a creature card exiled with Sisters of Stone Death onto the battlefield under your control."));
    }
}
