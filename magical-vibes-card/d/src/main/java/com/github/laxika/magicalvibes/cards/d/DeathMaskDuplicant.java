package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "115")
public class DeathMaskDuplicant extends Card {

    public DeathMaskDuplicant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ExileTargetCardFromGraveyardAndImprintOnSourceEffect(
                        new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                "{1}: Exile target creature card from your graveyard."
        ));
        addEffect(EffectSlot.STATIC, new GainKeywordsOfCreatureCardsExiledWithSourceEffect());
    }
}
