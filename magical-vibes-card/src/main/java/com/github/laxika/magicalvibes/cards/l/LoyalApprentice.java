package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "240")
public class LoyalApprentice extends Card {

    public LoyalApprentice() {
        // At the beginning of combat on your turn, if you control your commander,
        // create a 1/1 colorless Thopter artifact creature token with flying.
        // That token gains haste until end of turn.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControllerControlsCommander(),
                new CreateTokenEffect(
                        CardType.CREATURE,
                        1,
                        "Thopter",
                        1,
                        1,
                        null,
                        Set.of(),
                        List.of(CardSubtype.THOPTER),
                        Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT),
                        false,
                        false,
                        Map.of(),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of(Keyword.HASTE))));
    }
}
