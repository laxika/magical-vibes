package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TimesSourceRegeneratedThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceRegeneratedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "35")
public class SpinyStarfish extends Card {

    public SpinyStarfish() {
        // At the beginning of each end step, if this creature regenerated this turn, create a 0/1 blue
        // Starfish creature token for each time it regenerated this turn.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new SourceRegeneratedThisTurn(),
                new CreateTokenEffect(
                        new TimesSourceRegeneratedThisTurn(),
                        "Starfish",
                        0,
                        1,
                        CardColor.BLUE,
                        List.of(CardSubtype.STARFISH),
                        Set.of(),
                        Set.of()
                )
        ));

        // {U}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new RegenerateEffect()),
                "{U}: Regenerate this creature."
        ));
    }
}
