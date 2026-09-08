package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "108")
@CardRegistration(set = "TMT", collectorNumber = "208")
public class SlashReptileRampager extends Card {

    public SlashReptileRampager() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                "Mutant", 2, 2, CardColor.RED, List.of(CardSubtype.MUTANT), Set.of(), Set.of()));
    }
}
