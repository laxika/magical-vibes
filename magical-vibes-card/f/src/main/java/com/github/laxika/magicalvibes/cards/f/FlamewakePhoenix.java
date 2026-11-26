package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FDN", collectorNumber = "198")
@CardRegistration(set = "FRF", collectorNumber = "100")
public class FlamewakePhoenix extends Card {

    public FlamewakePhoenix() {
        var ferocious = new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentPowerAtLeastPredicate(4))));
        var returnToBattlefield = new PutCardsOntoBattlefieldEffect(Subjects.sourceObjectCard());
        var mayPay = UnlessPaysEffect.youMayPay("{R}", returnToBattlefield)
                .withPrompt("Pay {R} to return Flamewake Phoenix from your graveyard to the battlefield?");
        addStaticEffect(new MustAttackEffect());
        addTrigger(new TriggerCondition(Triggers.beginningOfCombat(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(ferocious),
                mayPay);
    }
}
