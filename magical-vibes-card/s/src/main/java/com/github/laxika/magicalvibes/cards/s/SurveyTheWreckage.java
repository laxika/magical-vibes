package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "107")
public class SurveyTheWreckage extends Card {

    public SurveyTheWreckage() {
        // Destroy target land. Create a 1/1 red Goblin creature token.
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect(false))
                .addSpellEffect(new CreateTokenEffect(1, "Goblin", 1, 1, CardColor.RED,
                        List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
