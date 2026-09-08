package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EmetSelchUnsundered;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/** Back face of {@link EmetSelchUnsundered}. */
public class HadesSorcererOfEld extends Card {

    public HadesSorcererOfEld() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(), new CastSpellsFromGraveyardEffect(new CardTruePredicate())));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(), new PlayLandsFromGraveyardEffect()));
        addEffect(EffectSlot.STATIC, new ExileOwnCardsInsteadOfGraveyardEffect());
    }
}
