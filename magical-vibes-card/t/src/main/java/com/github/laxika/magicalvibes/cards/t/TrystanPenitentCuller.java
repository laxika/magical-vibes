package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class TrystanPenitentCuller extends Card {

    public TrystanPenitentCuller() {
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS), SequenceEffect.of(
                new MillEffect(3, Subjects.you()),
                ExileUpToOneCardFromGraveyardEffect.matching(new CardSubtypePredicate(CardSubtype.ELF))
                        .from(Subjects.you()),
                new ConditionalEffect(HappenedThisWay.exiled(),
                        new LoseLifeEffect(2, Subjects.eachOpponent()))));
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                UnlessPaysEffect.youMayPay("{G}", new TransformSelfEffect())
                        .withPrompt("Pay {G} to transform Trystan?"));
    }
}
