package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

/**
 * Adanto, the First Fort — back face of Legion's Landing.
 * Legendary Land.
 * {T}: Add {W}.
 * {2}{W}, {T}: Create a 1/1 white Vampire creature token with lifelink.
 */
public class AdantoTheFirstFort extends Card {

    public AdantoTheFirstFort() {
        // {T}: Add {W}.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {W}."
        ));

        // {2}{W}, {T}: Create a 1/1 white Vampire creature token with lifelink.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{W}",
                List.of(new CreateTokenEffect("Vampire", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of())),
                "{2}{W}, {T}: Create a 1/1 white Vampire creature token with lifelink."
        ));
    }
}
