package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "117")
@CardRegistration(set = "WHO", collectorNumber = "404")
@CardRegistration(set = "WHO", collectorNumber = "722")
@CardRegistration(set = "WHO", collectorNumber = "995")
public class CultOfSkaro extends Card {

    public CultOfSkaro() {
        addTrigger(Triggers.attacks(), new ChooseOneAtRandomEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Thay — Put a +1/+1 counter on each artifact creature you control",
                        PutCountersEffect.onEach(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledBySourceControllerPredicate()
                                )))),
                new ChooseOneEffect.ChooseOneOption("Caan — Draw two cards", new DrawCardEffect(2)),
                new ChooseOneEffect.ChooseOneOption("Sec — Create a 3/3 black Dalek artifact creature token with menace",
                        new CreateTokenEffect("Dalek", 3, 3, CardColor.BLACK,
                                List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE), Set.of(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption("Jast — Each opponent loses 4 life",
                        new LoseLifeEffect(4, Subjects.eachOpponent()))
        )));
    }
}
