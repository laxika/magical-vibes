package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "227")
@CardRegistration(set = "FIN", collectorNumber = "492")
public class IgnisScientia extends Card {

    public IgnisScientia() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayPutOneMatchingOntoBattlefieldRestOnBottomRandom(
                        6, new CardTypePredicate(CardType.LAND)));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}{U}",
                List.of(new ExileGraveyardCardCreateTokenIfCreatureEffect(foodToken())),
                "{1}{G}{U}, {T}: Exile target card from a graveyard. If a creature card was exiled this way, create a Food token."
        ));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }
}
