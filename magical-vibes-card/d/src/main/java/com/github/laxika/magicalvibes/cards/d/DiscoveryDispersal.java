package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SurveilThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongControllerPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "223")
@CardRegistration(set = "MKC", collectorNumber = "207")
public class DiscoveryDispersal extends Card {

    public DiscoveryDispersal() {
        CardEffect discovery = SurveilThenEffect.direct(2, new DrawCardEffect(1));
        // Each opponent chooses, then the chosen permanents return together; then each chooses a card, and the
        // chosen cards are discarded together (its ruling).
        CardEffect dispersal = SequenceEffect.of(
                ChoosePermanentsEffect.eachChoosesOne(Subjects.eachOpponent(),
                        Subjects.controlledBy(Subjects.eachOpponent(), new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentHasGreatestManaValueAmongControllerPermanentsPredicate()))),
                        "a nonland permanent with the greatest mana value among permanents"),
                new ReturnToHandEffect(Subjects.thisWay(StepKind.CHOOSE)),
                new DiscardEffect(1, Subjects.eachOpponent()));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Discovery — Surveil 2, then draw a card", discovery).withManaCost("{1}{U/B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Dispersal — Each opponent returns a nonland permanent they control with the greatest mana value among permanents they control to its owner's hand, then discards a card",
                        dispersal).withManaCost("{3}{U}{B}")
        )));
    }
}
