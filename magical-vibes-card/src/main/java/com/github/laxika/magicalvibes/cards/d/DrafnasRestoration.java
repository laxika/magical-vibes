package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ATQ", collectorNumber = "8")
public class DrafnasRestoration extends Card {

    public DrafnasRestoration() {
        addEffect(EffectSlot.SPELL, PutTargetCardsFromGraveyardOnTopOfLibraryEffect.fromAnyPlayerGraveyard(
                new CardTypePredicate(CardType.ARTIFACT),
                PutTargetCardsFromGraveyardOnTopOfLibraryEffect.ANY_NUMBER));
    }
}
