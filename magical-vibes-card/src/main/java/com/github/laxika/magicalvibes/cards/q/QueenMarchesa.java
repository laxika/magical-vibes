package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnOpponentIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "499")
@CardRegistration(set = "SLD", collectorNumber = "1559")
public class QueenMarchesa extends Card {

    public QueenMarchesa() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AnOpponentIsMonarch(),
                new CreateTokenEffect(
                        1, "Assassin", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.ASSASSIN), Set.of(Keyword.DEATHTOUCH, Keyword.HASTE), Set.of())));
    }
}
