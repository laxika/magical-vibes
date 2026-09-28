package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "34")
@CardRegistration(set = "DSC", collectorNumber = "61")
public class FormlessGenesis extends Card {

    public FormlessGenesis() {
        CardsInGraveyard landCardsInGraveyard = new CardsInGraveyard(
                new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Shapeshifter",
                landCardsInGraveyard,
                landCardsInGraveyard,
                null,
                List.of(CardSubtype.SHAPESHIFTER),
                Set.of(Keyword.CHANGELING, Keyword.DEATHTOUCH),
                Set.of()));

        addCastingOption(new Retrace());
    }
}
