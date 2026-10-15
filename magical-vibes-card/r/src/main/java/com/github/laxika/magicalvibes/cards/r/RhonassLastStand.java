package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "132")
public class RhonassLastStand extends Card {

    public RhonassLastStand() {
        // Create a 5/4 green Snake creature token.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Snake", 5, 4, CardColor.GREEN,
                List.of(CardSubtype.SNAKE), Set.of(), Set.of()));

        // Lands you control don't untap during your next untap step.
        addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
    }
}
