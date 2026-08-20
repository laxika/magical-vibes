package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLookAtHandAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "227")
public class TheRavensWarning extends Card {

    public TheRavensWarning() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                1, "Bird", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new GainLifeEffect(2));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new RegisterDelayedCombatDamageLookAtHandAndDrawEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new SearchOutsideGameForCardToTopOfLibraryEffect());
    }
}
