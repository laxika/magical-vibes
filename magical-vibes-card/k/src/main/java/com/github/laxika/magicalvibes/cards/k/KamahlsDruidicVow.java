package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "166")
public class KamahlsDruidicVow extends Card {

    public KamahlsDruidicVow() {
        // Land cards are always eligible; legendary permanent cards are eligible if MV <= X
        addEffect(EffectSlot.SPELL, new LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect(
                new CardTypePredicate(CardType.LAND),
                new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new CardIsPermanentPredicate()
                ))
        ));
    }
}
