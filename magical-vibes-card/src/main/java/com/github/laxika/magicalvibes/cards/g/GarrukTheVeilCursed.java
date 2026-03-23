package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureSearchLibraryForCreatureToHandEffect;

import java.util.List;
import java.util.Set;

/**
 * Garruk, the Veil-Cursed — back face of Garruk Relentless.
 * Legendary Planeswalker — Garruk (Black, Green).
 */
public class GarrukTheVeilCursed extends Card {

    public GarrukTheVeilCursed() {
        // +1: Create a 1/1 black Wolf creature token with deathtouch.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect("Wolf", 1, 1,
                        CardColor.BLACK, List.of(CardSubtype.WOLF),
                        Set.of(Keyword.DEATHTOUCH), Set.of())),
                "+1: Create a 1/1 black Wolf creature token with deathtouch."
        ));

        // −1: Sacrifice a creature. If you do, search your library for a creature card,
        // reveal it, put it into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new SacrificeCreatureSearchLibraryForCreatureToHandEffect()),
                "\u22121: Sacrifice a creature. If you do, search your library for a creature card, reveal it, put it into your hand, then shuffle."
        ));

        // −3: Creatures you control gain trample and get +X/+X until end of turn,
        // where X is the number of creature cards in your graveyard.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES),
                        new BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect()
                ),
                "\u22123: Creatures you control gain trample and get +X/+X until end of turn, where X is the number of creature cards in your graveyard."
        ));
    }
}
