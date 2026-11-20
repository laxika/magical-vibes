package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "115")
public class DeathMaskDuplicant extends Card {

    public DeathMaskDuplicant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        ExileGraveyardCardsEffect.oneTargetFromControllerGraveyardWithSource(
                                        new CardTypePredicate(CardType.CREATURE))
                                .imprintingOnSource()),
                "{1}: Exile target creature card from your graveyard."
        ));
        addStaticEffect(new GainKeywordsOfCreatureCardsExiledWithSourceEffect());
    }
}
