package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "23")
public class DeathTyrant extends Card {

    public DeathTyrant() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(), CreateTokenEffect.blackZombie(1)));
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), new TriggeringPermanentConditionalEffect(
                new PermanentIsBlockingPredicate(), CreateTokenEffect.blackZombie(1)));

        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{5}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .enterTapped(true)
                        .build()),
                "{5}{B}: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
