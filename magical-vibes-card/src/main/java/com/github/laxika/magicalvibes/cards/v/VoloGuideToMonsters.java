package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithControlledCreatureOrGraveyardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "238")
public class VoloGuideToMonsters extends Card {

    public VoloGuideToMonsters() {
        CardAllOfPredicate qualifyingCreatureSpell = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardSharesCreatureTypeWithControlledCreatureOrGraveyardPredicate())
        ));

        // Copying a qualifying creature spell creates a token and does not offer new targets.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopyControllerCastSpellOnSpellCastEffect(
                        qualifyingCreatureSpell,
                        null,
                        null,
                        null,
                        null,
                        Set.of(),
                        null,
                        Set.of(),
                        true,
                        false
                ));
    }
}
