package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "14")
@CardRegistration(set = "OPCA", collectorNumber = "26")
public class FurnaceLayer extends Card {

    public FurnaceLayer() {
        CardEffect discardEffect = SequenceEffect.of(
                new DiscardEffect(1, Subjects.targetPlayerChosenAtRandom()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.DISCARD, new CardTypePredicate(CardType.LAND)),
                        new LoseLifeEffect(3, Subjects.targetPlayerDeclaredElsewhere())));
        addTrigger(Triggers.planeswalkToThis(), discardEffect);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), discardEffect);

        target(TargetFilters.nonlandPermanent()).addTrigger(Triggers.chaosEnsues(),
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target nonland permanent?"));
    }
}
