package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EmetSelchUnsundered;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/** Back face of {@link EmetSelchUnsundered}. */
public class HadesSorcererOfEld extends Card {

    public HadesSorcererOfEld() {
        addStaticEffect(new ConditionalEffect(
                new ControllerTurn(), new CastSpellsFromGraveyardEffect(new CardTruePredicate())));
        addStaticEffect(new ConditionalEffect(
                new ControllerTurn(), new PlayLandsFromGraveyardEffect()));
        addStaticEffect(new ExileOwnCardsInsteadOfGraveyardEffect(true));
    }
}
