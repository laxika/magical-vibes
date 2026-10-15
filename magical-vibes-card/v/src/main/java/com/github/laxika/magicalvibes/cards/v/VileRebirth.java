package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M13", collectorNumber = "115")
@CardRegistration(set = "M14", collectorNumber = "121")
public class VileRebirth extends Card {

    public VileRebirth() {
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(
                1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD, new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombie(1));
    }
}
