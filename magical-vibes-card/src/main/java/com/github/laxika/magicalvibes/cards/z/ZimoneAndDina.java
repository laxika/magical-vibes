package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "257")
public class ZimoneAndDina extends Card {

    public ZimoneAndDina() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(2)));

        CardTypePredicate landCard = new CardTypePredicate(CardType.LAND);
        CardEffect process = SequenceEffect.of(
                new DrawCardEffect(),
                new MayEffect(
                        new PutCardToBattlefieldEffect(landCard, "land", true),
                        "Put a land card from your hand onto the battlefield tapped?"
                ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        process,
                        new ConditionalEffect(
                                new ControlsPermanentCount(8, new PermanentIsLandPredicate()),
                                process
                        )
                ),
                "{T}, Sacrifice another creature: Draw a card. You may put a land card from your hand "
                        + "onto the battlefield tapped. If you control eight or more lands, repeat this process once."
        ));
    }
}
