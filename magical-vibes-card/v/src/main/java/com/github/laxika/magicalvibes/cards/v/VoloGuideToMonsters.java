package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithControlledCreatureOrGraveyardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "AFR", collectorNumber = "238")
public class VoloGuideToMonsters extends Card {

    public VoloGuideToMonsters() {
        CardAllOfPredicate qualifyingCreatureSpell = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardSharesCreatureTypeWithControlledCreatureOrGraveyardPredicate())
        ));

        // Copying a qualifying creature spell creates a token and does not offer new targets.
        addTrigger(Triggers.youCastSpell().withFilter(qualifyingCreatureSpell),
                new CopyTriggeringSpellEffect(true).withoutNewTargetChoice());
    }
}
