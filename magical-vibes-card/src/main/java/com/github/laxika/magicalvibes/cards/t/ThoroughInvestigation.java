package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "AFC", collectorNumber = "12")
public class ThoroughInvestigation extends Card {

    public ThoroughInvestigation() {
        // Whenever you attack, investigate.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, CreateTokenEffect.ofClueToken(1));

        // Whenever you sacrifice a Clue, venture into the dungeon.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                        new VentureIntoDungeonEffect()
                ));
    }
}
