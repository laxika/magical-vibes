package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TopCardOfLibraryColorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "118")
public class VampireNocturnus extends Card {

    public VampireNocturnus() {
        // Play with the top card of your library revealed.
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        // As long as the top card of your library is black, Vampire Nocturnus and other
        // Vampire creatures you control each get +2/+1 and have flying.
        addEffect(EffectSlot.STATIC, new TopCardOfLibraryColorConditionalEffect(
                CardColor.BLACK,
                new StaticBoostEffect(2, 1, Set.of(Keyword.FLYING), GrantScope.OWN_CREATURES,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.VAMPIRE)))
        ));
    }
}
