package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "71")
public class OracleOfTragedy extends Card {

    public OracleOfTragedy() {
        ChooseOneEffect triggeredAbility = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Draw a card, then discard a card",
                        List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER))),
                new ChooseOneEffect.ChooseOneOption(
                        "Shuffle up to four target cards with mana value 3 or greater from your graveyard into your library",
                        new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(
                                new CardMinManaValuePredicate(3), 4))
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, triggeredAbility);
        addEffect(EffectSlot.ON_DEATH, triggeredAbility);
    }
}
