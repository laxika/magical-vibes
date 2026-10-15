package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasMorphAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "35")
public class Dermoplasm extends Card {

    public Dermoplasm() {
        addMorph("{2}{U}{U}");

        CardAllOfPredicate creatureWithMorph = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardHasMorphAbilityPredicate()));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new MayEffect(
                new PutCardToBattlefieldThenEffect(
                        creatureWithMorph, "creature with a morph ability", null,
                        ReturnToHandEffect.self()),
                "Put a creature card with a morph ability from your hand onto the battlefield?"));
    }
}
