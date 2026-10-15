package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "56")
public class ReservoirKraken extends Card {

    public ReservoirKraken() {
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new AnyOpponentMayTapCreatureTapAndCreateTokenSourceEffect(fishToken()));
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(
                1,
                "Fish",
                1,
                1,
                CardColor.BLUE,
                List.of(CardSubtype.FISH),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CantBeBlockedEffect()));
    }
}
