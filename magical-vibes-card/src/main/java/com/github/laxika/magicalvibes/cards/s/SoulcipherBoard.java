package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CipherboundSpirit;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

/**
 * Soulcipher Board — front face of Soulcipher Board // Cipherbound Spirit.
 * Artifact {1}{U}
 * This artifact enters with three omen counters on it.
 * {1}{U}, {T}: Look at the top two cards of your library. Put one of them into your graveyard.
 * Whenever a creature card is put into your graveyard from anywhere, remove an omen counter from
 * this artifact. Then if it has no omen counters on it, transform it.
 */
@CardRegistration(set = "INR", collectorNumber = "85")
public class SoulcipherBoard extends Card {

    public SoulcipherBoard() {
        CipherboundSpirit backFace = new CipherboundSpirit();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // This artifact enters with three omen counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.OMEN, new Fixed(3)));

        // {1}{U}, {T}: Look at the top two cards of your library. Put one of them into your graveyard.
        // No target — falls back to the controller's own library (same as Puresight Merrow).
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new LookAtTopCardsOfTargetLibraryEffect(2, TargetLibraryAction.PUT_ONE_INTO_GRAVEYARD)),
                "{1}{U}, {T}: Look at the top two cards of your library. Put one of them into your graveyard."
        ));

        // Whenever a creature card is put into your graveyard from anywhere, remove an omen counter
        // from this artifact. Then if it has no omen counters on it, transform it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, SequenceEffect.of(
                new RemoveCounterFromSourceEffect(CounterType.OMEN, 1),
                new ConditionalEffect(
                        new NotCondition(new SourceCounterThreshold(1, CounterType.OMEN)),
                        new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "CipherboundSpirit";
    }
}
