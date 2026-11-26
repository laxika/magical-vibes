package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "137")
public class ReignOfTerror extends Card {

    public ReignOfTerror() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Destroy all green creatures", wipe(CardColor.GREEN)),
                new ChooseOneEffect.ChooseOneOption("Destroy all white creatures", wipe(CardColor.WHITE))
        )));
    }

    private static SequenceEffect wipe(final CardColor color) {
        final PermanentPredicate filter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(color))
        ));
        return SequenceEffect.of(DestroyTargetPermanentEffect.all(filter, true),
                new LoseLifeEffect(new Scaled(AmountThisWay.died(StepKind.DESTROY), 2), Subjects.you()));
    }
}
