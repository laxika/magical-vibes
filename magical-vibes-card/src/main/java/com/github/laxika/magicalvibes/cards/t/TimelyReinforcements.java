package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreLifeThanController;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "40")
public class TimelyReinforcements extends Card {

    public TimelyReinforcements() {
        // If you have less life than an opponent, you gain 6 life.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AnOpponentHasMoreLifeThanController(), new GainLifeEffect(6)));
        // If you control fewer creatures than an opponent, create three 1/1 white Soldier creature tokens.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new OpponentControlsMoreCreatures(1),
                new CreateTokenEffect(3, "Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of())));
    }
}
