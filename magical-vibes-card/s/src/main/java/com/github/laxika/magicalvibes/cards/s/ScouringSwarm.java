package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOC", collectorNumber = "16")
@CardRegistration(set = "EOC", collectorNumber = "36")
public class ScouringSwarm extends Card {

    public ScouringSwarm() {
        GraveyardCardThreshold sevenLandCards = new GraveyardCardThreshold(
                7, new CardTypePredicate(CardType.LAND));
        PermanentIsLandPredicate sacrificedLand = new PermanentIsLandPredicate();

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(sacrificedLand,
                        ConditionalEffect.unless(sevenLandCards,
                                CreateTokenCopyOfSourceEffect.tapped(1))));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(sacrificedLand,
                        ConditionalEffect.unless(new NotCondition(sevenLandCards),
                                new CreateTokenEffect(1, "Insect", 1, 1, CardColor.BLACK,
                                        List.of(CardSubtype.INSECT), Set.of(Keyword.FLYING), Set.of(), true))));
    }
}
