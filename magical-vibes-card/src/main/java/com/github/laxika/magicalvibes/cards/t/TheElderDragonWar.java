package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "121")
public class TheElderDragonWar extends Card {

    public TheElderDragonWar() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToEachMatchingPermanentEffect(
                2, new PermanentIsCreaturePredicate(), EachPermanentScope.ALL_PLAYERS));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToPlayersEffect(
                2, DamageRecipient.EACH_OPPONENT));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect());

        addEffect(EffectSlot.SAGA_CHAPTER_III, new CreateTokenEffect(
                "Dragon", 4, 4, CardColor.RED, List.of(CardSubtype.DRAGON),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
