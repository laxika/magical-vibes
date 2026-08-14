package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "77")
public class ZulAshurLichLord extends Card {

    public ZulAshurLichLord() {
        // Ward—Pay 2 life.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(0, 2));

        // {T}: You may cast target Zombie creature card from your graveyard this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AllowCastTargetCardFromGraveyardThisTurnEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.ZOMBIE))),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false)),
                "{T}: You may cast target Zombie creature card from your graveyard this turn."
        ));
    }
}
