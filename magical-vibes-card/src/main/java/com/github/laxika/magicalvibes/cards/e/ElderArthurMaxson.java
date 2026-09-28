package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "102")
@CardRegistration(set = "PIP", collectorNumber = "413")
@CardRegistration(set = "PIP", collectorNumber = "630")
@CardRegistration(set = "PIP", collectorNumber = "941")
public class ElderArthurMaxson extends Card {

    public ElderArthurMaxson() {
        // Creature tokens you control have training.
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.TRAINING, GrantScope.OWN_CREATURES,
                        new PermanentIsTokenPredicate()));

        // Sacrifice another creature: Elder Arthur Maxson gains indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)
                ),
                "Sacrifice another creature: Elder Arthur Maxson gains indestructible until end of turn."
        ));
    }
}
