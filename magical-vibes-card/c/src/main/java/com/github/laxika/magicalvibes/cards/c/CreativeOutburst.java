package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "171")
public class CreativeOutburst extends Card {

    public CreativeOutburst() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(5));
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(new Fixed(5), 1));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{U/R}{U/R}",
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{U/R}{U/R}, Discard this card: Create a Treasure token."));
    }
}
