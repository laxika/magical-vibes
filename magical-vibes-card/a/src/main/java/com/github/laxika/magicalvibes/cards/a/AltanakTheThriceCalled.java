package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "166")
public class AltanakTheThriceCalled extends Card {

    public AltanakTheThriceCalled() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new DrawCardEffect());

        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.LAND))
                        .targetGraveyard(true)
                        .enterTapped(true)
                        .build()),
                "{1}{G}, Discard this card: Return target land card from your graveyard to the battlefield tapped."
        ));
    }
}
