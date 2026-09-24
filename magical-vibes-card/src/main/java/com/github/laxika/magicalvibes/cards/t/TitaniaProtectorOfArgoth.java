package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "287")
public class TitaniaProtectorOfArgoth extends Card {

    public TitaniaProtectorOfArgoth() {
        // When Titania enters, return target land card from your graveyard to the battlefield.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.LAND))
                .targetGraveyard(true)
                .build());

        // Whenever a land you control is put into a graveyard from the battlefield, create a 5/3
        // green Elemental creature token.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.LAND),
                        new CreateTokenEffect("Elemental", 5, 3, CardColor.GREEN,
                                List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of())));
    }
}
