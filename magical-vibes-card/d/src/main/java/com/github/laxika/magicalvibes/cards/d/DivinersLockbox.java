package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "225")
public class DivinersLockbox extends Card {

    public DivinersLockbox() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ChooseCardNameAtResolutionEffect(),
                        new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, named),
                                SequenceEffect.of(new SacrificeSelfEffect(), new DrawCardEffect(3)))),
                "{1}, {T}: Choose a card name, then reveal the top card of your library. If that card has the chosen name, sacrifice this artifact and draw three cards. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
