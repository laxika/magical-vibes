package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "22")
public class OketrasAttendant extends Card {

    public OketrasAttendant() {
        // Flying — intrinsic keyword, auto-loaded from Scryfall.

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new DrawCardEffect(1)),
                "Cycling {2} ({2}, Discard this card: Draw a card.)"));

        // Embalm {3}{W}{W} ({3}{W}{W}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Bird Soldier with no mana cost. Embalm only as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)
                ),
                "Embalm {3}{W}{W} ({3}{W}{W}, Exile this card from your graveyard: Create a token that's a copy of it, "
                        + "except it's a white Zombie Bird Soldier with no mana cost. Embalm only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
