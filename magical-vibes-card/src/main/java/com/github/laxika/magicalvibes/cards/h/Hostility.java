package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PreventSpellDamageToOpponentAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "176")
public class Hostility extends Card {

    public Hostility() {
        // Haste is auto-loaded from Scryfall.
        // "If a spell you control would deal damage to an opponent, prevent that damage. Create a 3/1 red
        // Elemental Shaman creature token with haste for each 1 damage prevented this way."
        addEffect(EffectSlot.STATIC, new PreventSpellDamageToOpponentAndCreateTokensEffect(
                new CreateTokenEffect(1, "Elemental Shaman", 3, 1, CardColor.RED,
                        List.of(CardSubtype.ELEMENTAL, CardSubtype.SHAMAN), Set.of(Keyword.HASTE), Set.of())));

        // "When Hostility is put into a graveyard from anywhere, shuffle it into its owner's library."
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
