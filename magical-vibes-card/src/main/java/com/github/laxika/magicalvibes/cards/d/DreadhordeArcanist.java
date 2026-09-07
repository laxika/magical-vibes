package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "125")
public class DreadhordeArcanist extends Card {

    public DreadhordeArcanist() {
        addEffect(EffectSlot.ON_ATTACK, new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                true,
                true,
                new CardAllOfPredicate(List.of(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        new CardManaValueAtMostSourcePowerPredicate()))));
    }
}
