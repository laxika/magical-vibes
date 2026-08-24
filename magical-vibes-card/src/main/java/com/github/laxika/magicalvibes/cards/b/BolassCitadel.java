package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryByPayingLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "79")
public class BolassCitadel extends Card {

    public BolassCitadel() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new PlayLandsFromTopOfLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryByPayingLifeEqualToManaValueEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(10,
                                new PermanentNotPredicate(new PermanentIsLandPredicate())),
                        new LoseLifeEffect(10, LoseLifeRecipient.EACH_OPPONENT)
                ),
                "{T}, Sacrifice ten nonland permanents: Each opponent loses 10 life."
        ));
    }
}
