package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "136")
@CardRegistration(set = "ME2", collectorNumber = "224")
public class WhirlingCatapult extends Card {

    public WhirlingCatapult() {
        // {2}, Exile the top two cards of your library: This artifact deals 1 damage
        // to each creature with flying and each player.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileTopCardOfLibraryCost(2),
                        DealDamageEffect.toEachCreature(1,
                                new PermanentHasKeywordPredicate(Keyword.FLYING)).andTo(Subjects.eachPlayer())
                ),
                "{2}, Exile the top two cards of your library: This artifact deals 1 damage "
                        + "to each creature with flying and each player."
        ));
    }
}
