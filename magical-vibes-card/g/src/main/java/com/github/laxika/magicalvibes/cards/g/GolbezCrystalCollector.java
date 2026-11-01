package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RecordReturnedGraveyardCardValueEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnedGraveyardCardValue;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "225")
@CardRegistration(set = "FIN", collectorNumber = "395")
@CardRegistration(set = "FIN", collectorNumber = "490")
@CardRegistration(set = "FIN", collectorNumber = "540")
public class GolbezCrystalCollector extends Card {

    public GolbezCrystalCollector() {
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL), new SurveilEffect(1));

        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
        PermanentIsArtifactPredicate artifact = new PermanentIsArtifactPredicate();

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(4, artifact),
                SequenceEffect.of(
                        returnCreature,
                        new RecordReturnedGraveyardCardValueEffect(ReturnedGraveyardCardValue.POWER),
                        new ConditionalEffect(
                                new ControlsPermanentCount(8, artifact),
                                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT)))));
    }
}
