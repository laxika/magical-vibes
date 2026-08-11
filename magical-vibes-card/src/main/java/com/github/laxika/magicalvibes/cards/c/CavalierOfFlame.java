package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "125")
public class CavalierOfFlame extends Card {

    public CavalierOfFlame() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 0),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES)
                ),
                "{1}{R}: Creatures you control get +1/+0 and gain haste until end of turn."
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardUpToThenDrawThatManyEffect(DiscardUpToThenDrawThatManyEffect.ANY_NUMBER));

        CardsInGraveyard landsInGraveyard = new CardsInGraveyard(
                new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new DealDamageToPlayersEffect(landsInGraveyard, DamageRecipient.EACH_OPPONENT),
                new MassDamageEffect(landsInGraveyard, false, true,
                        new PermanentIsPlaneswalkerPredicate())));
    }
}
