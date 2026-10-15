package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "194")
public class ViralSpawning extends Card {

    public ViralSpawning() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Phyrexian Beast",
                3,
                3,
                CardColor.GREEN,
                null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.BEAST),
                Set.of(Keyword.TOXIC),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
        addCastingOption(new FlashbackCast("{2}{G}", new OpponentPoisoned(3)));
    }
}
