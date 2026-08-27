package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "239")
@CardRegistration(set = "FIN", collectorNumber = "504")
public class RydiaSummonerOfMist extends Card {

    public RydiaSummonerOfMist() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new MayEffect(
                new DiscardCardThenEffect(null, new DrawCardEffect(1), "a card"),
                "Discard a card to draw a card?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardSubtypePredicate(CardSubtype.SAGA))
                        .targetGraveyard(true)
                        .requiresManaValueEqualsX(true)
                        .enterWithCounter(CounterType.FINALITY)
                        .enterWithCounterCount(1)
                        .grantHaste(true)
                        .build()),
                "{X}, {T}: Return target Saga card with mana value X from your graveyard to the battlefield "
                        + "with a finality counter on it. It gains haste until end of turn. Activate only as "
                        + "a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
