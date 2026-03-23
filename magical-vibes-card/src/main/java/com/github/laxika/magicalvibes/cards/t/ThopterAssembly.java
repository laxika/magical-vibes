package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NoOtherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAndCreateTokensEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "140")
public class ThopterAssembly extends Card {

    public ThopterAssembly() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new NoOtherSubtypeConditionalEffect(
                CardSubtype.THOPTER,
                new ReturnSelfToHandAndCreateTokensEffect(
                        new CreateTokenEffect(5, "Thopter", 1, 1,
                                null, List.of(CardSubtype.THOPTER),
                                Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT))
                )
        ));
    }
}
