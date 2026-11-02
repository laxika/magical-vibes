package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsExiledWithPlanarSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTriggeringPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayedCardExiledWithPlanarSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "589")
public class TheMatrixOfTime extends Card {

    public TheMatrixOfTime() {
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED,
                new ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect());
        addStaticEffect(new AllowPlayCardsExiledWithPlanarSourceEffect());

        CardEffect playedCardFollowUp = SequenceEffect.of(
                new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PLAYER),
                new ExileTopCardOfTriggeringPlayerLibraryEffect());
        addTrigger(Triggers.youCastSpell(),
                new PlayedCardExiledWithPlanarSourceTriggerEffect(playedCardFollowUp));
        addTrigger(Triggers.youPlayLand(),
                new PlayedCardExiledWithPlanarSourceTriggerEffect(playedCardFollowUp));
        addEffect(EffectSlot.CHAOS_TRIGGERED, CreateTokenEffect.ofTreasureToken(2));
    }
}
