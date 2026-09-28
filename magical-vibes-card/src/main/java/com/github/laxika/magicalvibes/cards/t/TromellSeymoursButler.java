package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "73")
@CardRegistration(set = "FIC", collectorNumber = "162")
public class TromellSeymoursButler extends Card {

    public TromellSeymoursButler() {
        var nontokenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        ));
        addEffect(EffectSlot.STATIC,
                new ControlledPermanentsEnterWithAdditionalCountersEffect(nontokenCreature, 1));

        var nontokenCreatureCard = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardIsTokenPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ProliferateEffect(new PermanentsEnteredBattlefieldThisTurn(
                        nontokenCreatureCard, CountScope.CONTROLLER))),
                "{1}, {T}: Proliferate X times, where X is the number of nontoken creatures you control that entered this turn."
        ));
    }
}
