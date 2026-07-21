package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;
import java.util.Set;

/**
 * Wedding Announcement — front face of Wedding Announcement // Wedding Festivity.
 * {2}{W} Enchantment
 * At the beginning of your end step, put an invitation counter on this enchantment.
 * If you attacked with two or more creatures this turn, draw a card. Otherwise, create
 * a 1/1 white Human creature token. Then if this enchantment has three or more
 * invitation counters on it, transform it.
 */
@CardRegistration(set = "INR", collectorNumber = "51")
public class WeddingAnnouncement extends Card {

    public WeddingAnnouncement() {
        WeddingFestivity backFace = new WeddingFestivity();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // At the beginning of your end step, put an invitation counter on this enchantment.
        // If you attacked with two or more creatures this turn, draw a card. Otherwise, create
        // a 1/1 white Human creature token. Then if this enchantment has three or more
        // invitation counters on it, transform it.
        // Counters are kept on transform (ruling).
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.INVITATION),
                new ConditionalReplacementEffect(
                        new AttackedWithCreaturesThisTurn(2),
                        new CreateTokenEffect("Human", 1, 1,
                                CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()),
                        new DrawCardEffect(1)),
                new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.INVITATION),
                        new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "WeddingFestivity";
    }
}
