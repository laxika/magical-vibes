package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "6")
public class GelidShackles extends Card {

    public GelidShackles() {
        target(TargetFilters.creature())
                .addStaticEffect(new CantBlockEffect())
                .addStaticEffect(EnchantedPermanentCantActivateAbilitiesEffect.includingManaAbilities());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{S}",
                List.of(new GrantKeywordEffect(
                        Keyword.DEFENDER, Subjects.attachedCreature(), Duration.UNTIL_END_OF_TURN)),
                "{S}: Enchanted creature gains defender until end of turn."));
    }
}
