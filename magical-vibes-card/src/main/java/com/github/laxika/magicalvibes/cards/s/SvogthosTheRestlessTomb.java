package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "283")
public class SvogthosTheRestlessTomb extends Card {

    public SvogthosTheRestlessTomb() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        CardsInGraveyard creatureCards = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{G}",
                List.of(AnimatePermanentsEffect.withDynamicPowerToughness(
                        creatureCards, creatureCards,
                        List.of(CardSubtype.PLANT, CardSubtype.ZOMBIE), Set.of(),
                        Set.of(CardColor.BLACK, CardColor.GREEN))),
                "{3}{B}{G}: Until end of turn, this land becomes a black and green Plant Zombie creature with this creature's power and toughness each equal to the number of creature cards in your graveyard. It's still a land."
        ));
    }
}
