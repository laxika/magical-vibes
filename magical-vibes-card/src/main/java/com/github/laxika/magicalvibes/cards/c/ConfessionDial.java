package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "173")
public class ConfessionDial extends Card {

    public ConfessionDial() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(3));

        var legendaryCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSupertypePredicate(CardSupertype.LEGENDARY)));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantTargetGraveyardCardCastEffect(
                        legendaryCreature,
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false,
                        0,
                        false,
                        true)),
                "{T}: Target legendary creature card in your graveyard gains escape until end of turn. "
                        + "The escape cost is equal to its mana cost plus exile three other cards from your graveyard.",
                new GraveyardCardPredicateTargetFilter(
                        legendaryCreature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)));
    }
}
