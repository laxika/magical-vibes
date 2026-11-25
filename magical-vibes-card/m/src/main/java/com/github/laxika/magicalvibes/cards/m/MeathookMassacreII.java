package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "108")
public class MeathookMassacreII extends Card {

    public MeathookMassacreII() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SacrificePermanentsEffect(new XValue(), new PermanentIsCreaturePredicate(),
                        Subjects.eachPlayer()).withSimultaneousChoices());

        ReturnDyingCreatureToBattlefieldEffect returnCreature = new ReturnDyingCreatureToBattlefieldEffect(
                false, CounterType.FINALITY, 1);
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                UnlessPaysEffect.youMayPayLife(3, returnCreature)
                        .withPrompt("Pay 3 life to return that card to the battlefield under your control?"));

        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                UnlessPaysEffect.unlessPaysLife(Subjects.triggeringPlayer(), 3, returnCreature)
                        .withPrompt("Pay 3 life to keep that card in its graveyard?"));
    }
}
