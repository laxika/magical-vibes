package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "96")
public class OldOneEye extends Card {

    public OldOneEye() {
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.otherCreaturesYouControl()));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                "Tyranid", 5, 5, CardColor.GREEN, List.of(CardSubtype.TYRANID), Set.of(), Set.of()));
        // "You may discard two cards. If you do, return this card": both discards and the return
        // resolve within the one resolution, and the prompt is skipped with fewer than two cards.
        addTrigger(new TriggerCondition(Triggers.precombatMainPhase(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, new MayEffect(
                new ConditionalEffect(new CardsInHandAtLeast(2),
                        SequenceEffect.of(
                                new DiscardEffect(2, Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.atLeast(StepKind.DISCARD, 2),
                                        new ReturnSourceCardFromGraveyardToOwnerHandEffect()))),
                "Discard two cards to return Old One Eye to your hand?"));
    }
}
