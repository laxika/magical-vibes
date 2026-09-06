package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "145")
public class WhisperwoodElemental extends Card {

    public WhisperwoodElemental() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ManifestTopCardEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                                EffectSlot.ON_DEATH,
                                new ManifestTopCardEffect(),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentNotPredicate(new PermanentIsFaceDownPredicate()),
                                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))))),
                "Sacrifice this creature: Until end of turn, face-up nontoken creatures you control gain "
                        + "\"When this creature dies, manifest the top card of your library.\""
        ));
    }
}
