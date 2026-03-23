package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "194")
public class PrecursorGolem extends Card {

    public PrecursorGolem() {
        // When Precursor Golem enters the battlefield, create two 3/3 colorless Golem artifact creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                2, "Golem", 3, 3, null, List.of(CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT)));

        // Whenever a player casts an instant or sorcery spell that targets only a single Golem,
        // that player copies that spell for each other Golem that spell could target.
        // Each copy targets a different one of those Golems.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM));
    }
}
