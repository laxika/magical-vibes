package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "183")
public class KinTreeInvocation extends Card {

    public KinTreeInvocation() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                new Fixed(1),
                "Spirit Warrior",
                new GreatestToughnessAmongControlled(),
                new GreatestToughnessAmongControlled(),
                CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.SPIRIT, CardSubtype.WARRIOR),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
    }
}
