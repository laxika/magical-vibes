package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

/**
 * Aqueous Aria — the prepare spell (inset) of Campus Composer // Aqueous Aria (SOS 40).
 * <p>
 * Sorcery: Create a 3/3 blue and red Elemental creature token with flying.
 * <p>
 * Not independently registered: its oracle data is registered for the class name "AqueousAria" when
 * Campus Composer (SOS 40) loads (see {@code CampusComposerAqueousAria#getBackFaceClassName}). A copy of
 * this spell is created in exile while Campus Composer is prepared and may be cast from there.
 */
public class AqueousAria extends Card {

    public AqueousAria() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(1, "Elemental", 3, 3,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING), Set.of()));
    }
}
