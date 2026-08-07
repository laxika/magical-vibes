package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "276")
public class ForbiddenOrchard extends Card {

    public ForbiddenOrchard() {
        // {T}: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        // Whenever you tap this land for mana, target opponent creates a 1/1 colorless
        // Spirit creature token.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect("Spirit", 1, 1, null,
                        List.of(CardSubtype.SPIRIT), Set.of(), Set.of())
        ));
    }
}
