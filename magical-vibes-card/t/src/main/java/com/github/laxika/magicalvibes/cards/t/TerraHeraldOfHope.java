package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "4")
@CardRegistration(set = "FIC", collectorNumber = "186")
@CardRegistration(set = "FIC", collectorNumber = "204")
@CardRegistration(set = "FIC", collectorNumber = "212")
@CardRegistration(set = "FIC", collectorNumber = "223")
public class TerraHeraldOfHope extends Card {

    public TerraHeraldOfHope() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new MillEffect(2, MillRecipient.CONTROLLER),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayPayManaEffect(
                "{2}",
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtMostPredicate(3)
                        )))
                        .targetGraveyard(true)
                        .enterTapped(true)
                        .build(),
                "Pay {2} to return target creature card with power 3 or less from your graveyard to the battlefield tapped?"));
    }
}
