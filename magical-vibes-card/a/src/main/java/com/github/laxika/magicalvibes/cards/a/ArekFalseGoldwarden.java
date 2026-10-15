package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyNamedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "17")
public class ArekFalseGoldwarden extends Card {

    private static final String CARD_NAME = "Arek, False Goldwarden";

    public ArekFalseGoldwarden() {
        // Whenever another creature you control enters, cards you own named Arek, False
        // Goldwarden intensify by 1.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new IntensifyNamedCardsEffect(CARD_NAME));

        // {3}{W}{B}, {T}, Sacrifice Arek, False Goldwarden: Target opponent loses X life and you
        // gain X life, where X is Arek's intensity.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new LoseLifeEffect(new SourceIntensity(), LoseLifeRecipient.TARGET_PLAYER),
                        new GainLifeEffect(new SourceIntensity())
                ),
                "{3}{W}{B}, {T}, Sacrifice Arek, False Goldwarden: Target opponent loses X life and "
                        + "you gain X life, where X is Arek's intensity.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
