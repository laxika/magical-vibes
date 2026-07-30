package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;

import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "176")
public class GarruksHorde extends Card {

    public GarruksHorde() {
        // "Play with the top card of your library revealed."
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        // "You may cast creature spells from the top of your library."
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE)));
    }
}
