package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "97")
public class BloodswornSquire extends Card {

    public BloodswornSquire() {
        setBackFaceCard(new BloodswornKnight());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                        new TapPermanentsEffect(TapUntapScope.SELF),
                        new ConditionalEffect(
                                new GraveyardCardThreshold(4, new CardTypePredicate(CardType.CREATURE)),
                                new TransformSelfEffect())
                ),
                "{1}{B}, Discard a card: This creature gains indestructible until end of turn. "
                        + "Tap it. Then if there are four or more creature cards in your graveyard, "
                        + "transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "BloodswornKnight";
    }
}
