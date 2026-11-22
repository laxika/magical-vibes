package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "95")
public class PrincessYue extends Card {

    public PrincessYue() {
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, Triggers.NONLAND)),
                        ObjectRelation.THIS),
                new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                        .underControlOf(Subjects.you())
                        .enteringTapped()
                        .enteringWith(new SetNameEffect("Moon", Subjects.target()),
                                new SetCardTypesEffect(Set.of(CardType.LAND), Subjects.target()),
                                new GrantActivatedAbilityEffect(ManaAbilities.tapFor(ManaColor.COLORLESS), Subjects.target())));
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new ScryEffect(2)), "{T}: Scry 2."));
    }
}
