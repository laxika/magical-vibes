package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "251")
@CardRegistration(set = "INR", collectorNumber = "436")
public class VilespawnSpider extends Card {

    public VilespawnSpider() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MillEffect(1, MillRecipient.CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}{U}",
                List.of(new SacrificeSelfCost(), new CreateTokenEffect(
                        new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                        "Insect", 1, 1, CardColor.GREEN, List.of(CardSubtype.INSECT),
                        Set.of(), Set.of()
                )),
                "{2}{G}{U}, {T}, Sacrifice this creature: Create a 1/1 green Insect creature token for each creature card in your graveyard.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
