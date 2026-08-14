package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "127")
public class GrindingStation extends Card {

    public GrindingStation() {
        // {T}, Sacrifice an artifact: Target player mills three cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new MillEffect(3, MillRecipient.TARGET_PLAYER)
                ),
                "{T}, Sacrifice an artifact: Target player mills three cards."
        ));

        // Whenever an artifact enters, you may untap Grinding Station.
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF),
                                "Untap Grinding Station?")));
    }
}
