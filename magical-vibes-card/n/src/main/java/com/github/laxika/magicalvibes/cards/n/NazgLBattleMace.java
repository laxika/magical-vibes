package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "510")
@CardRegistration(set = "LTC", collectorNumber = "554")
public class NazgLBattleMace extends Card {

    public NazgLBattleMace() {
        addStaticEffect(new GrantKeywordEffect(Set.of(Keyword.MENACE, Keyword.DEATHTOUCH),
                        Subjects.attachedCreature()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.attacks(),
                new SacrificePermanentsEffect(
                        1, new PermanentTruePredicate(), Subjects.defendingPlayer())),
                Subjects.attachedCreature()));

        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.sacrificed(Triggers.NONTOKEN, ObjectRelation.OPPONENT_CONTROLS),
                UnlessPaysEffect.unlessPaysLife(Subjects.controllerOf(Subjects.triggering()), 3,
                        new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards()).underControlOf(Subjects.you()))),
                Subjects.attachedCreature()));

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
