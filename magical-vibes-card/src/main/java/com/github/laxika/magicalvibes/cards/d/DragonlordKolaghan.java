package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNameInControllerGraveyardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "218")
public class DragonlordKolaghan extends Card {

    public DragonlordKolaghan() {
        // Other creatures you control have haste.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));

        // Whenever an opponent casts a creature or planeswalker spell with the same name as a card in
        // their graveyard, that player loses 10 life.
        CardAnyOfPredicate creatureOrPlaneswalker = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.PLANESWALKER)
        ));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new CasterLosesLifeOnSpellCastEffect(
                new CardAllOfPredicate(List.of(creatureOrPlaneswalker,
                        new CardNameInControllerGraveyardPredicate())), 10));
    }
}
