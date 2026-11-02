package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardToSourceAndMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardExiledWithSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "183")
@CardRegistration(set = "DBL", collectorNumber = "450")
public class VoltaicVisionary extends Card {

    public VoltaicVisionary() {
        setBackFaceCard(new VoltChargedBerserker());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER),
                        new ExileTopCardToSourceAndMayPlayThisTurnEffect()),
                "{T}: This creature deals 2 damage to you. Exile the top card of your library. You may play that card this turn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
        addTrigger(Triggers.youCastSpell(),
                new PlayedCardExiledWithSourceTriggerEffect());
        addTrigger(Triggers.youPlayLand(),
                new PlayedCardExiledWithSourceTriggerEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "VoltChargedBerserker";
    }
}
