package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "158")
public class TectonicGiant extends Card {

    public TectonicGiant() {
        addEffect(EffectSlot.ON_ATTACK, chooseMode());
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY, chooseMode());
    }

    private ChooseOneEffect chooseMode() {
        return new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "This creature deals 3 damage to each opponent.",
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Choose one of them. Until the end of your next turn, you may play that card.",
                        new ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(2))
        ));
    }
}
