package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHexproofFromOwnColorsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "245")
@CardRegistration(set = "ECL", collectorNumber = "380")
public class TamMindfulFirstYear extends Card {

    public TamMindfulFirstYear() {
        addEffect(EffectSlot.STATIC, new GrantHexproofFromOwnColorsEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new GrantColorUntilEndOfTurnEffect(CardColor.WHITE, true),
                        new GrantColorUntilEndOfTurnEffect(CardColor.BLUE, true),
                        new GrantColorUntilEndOfTurnEffect(CardColor.BLACK, true),
                        new GrantColorUntilEndOfTurnEffect(CardColor.RED, true),
                        new GrantColorUntilEndOfTurnEffect(CardColor.GREEN, true)
                ),
                "{T}: Target creature you control becomes all colors until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
