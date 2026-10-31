package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryColor;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "118")
@CardRegistration(set = "M13", collectorNumber = "113")
public class VampireNocturnus extends Card {

    public VampireNocturnus() {
        // Play with the top card of your library revealed.
        addStaticEffect(new PlayWithTopCardRevealedEffect());
        // As long as the top card of your library is black, Vampire Nocturnus and other
        // Vampire creatures you control each get +2/+1 and have flying.
        // Other Vampires only get the bonus while they remain Vampires...
        addStaticEffect(new ConditionalEffect(new TopCardOfLibraryColor(CardColor.BLACK),
                new StaticBoostEffect(2, 1, Set.of(Keyword.FLYING), GrantScope.OWN_CREATURES,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.VAMPIRE)))));
        // ...but Vampire Nocturnus names itself, so it always gets the bonus even if it
        // loses the Vampire subtype (the self-reference isn't filtered by creature type).
        addStaticEffect(new ConditionalEffect(new TopCardOfLibraryColor(CardColor.BLACK),
                new StaticBoostEffect(2, 1, Set.of(Keyword.FLYING), GrantScope.SELF)));
    }
}
