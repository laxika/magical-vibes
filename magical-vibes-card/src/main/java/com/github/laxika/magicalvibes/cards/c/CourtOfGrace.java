package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1338")
public class CourtOfGrace extends Card {

    public CourtOfGrace() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalReplacementEffect(
                new ControllerIsMonarch(),
                CreateTokenEffect.whiteSpirit(1),
                new CreateTokenEffect(
                        1, "Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of())));
    }
}
