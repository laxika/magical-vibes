package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostForTargetPlayerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "DMU", collectorNumber = "40")
public class AcademyLoremaster extends Card {

    public AcademyLoremaster() {
        // At the beginning of each player's draw step, that player may draw an additional card.
        // If they do, spells they cast this turn cost {2} more to cast.
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new DrawCardForTargetPlayerEffect(1),
                        new IncreaseSpellCostForTargetPlayerUntilEndOfTurnEffect(
                                new CardTruePredicate(), 2)),
                "Draw an additional card?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
    }
}
