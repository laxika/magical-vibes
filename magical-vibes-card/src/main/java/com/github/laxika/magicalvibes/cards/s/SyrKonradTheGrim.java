package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "107")
public class SyrKonradTheGrim extends Card {

    public SyrKonradTheGrim() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new MillEffect(1, MillRecipient.CONTROLLER),
                        new MillEffect(1, MillRecipient.EACH_OPPONENT)),
                "{1}{B}: Each player mills a card."
        ));
    }
}
