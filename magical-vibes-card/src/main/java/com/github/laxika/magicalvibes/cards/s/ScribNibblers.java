package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "66")
public class ScribNibblers extends Card {

    public ScribNibblers() {
        // {T}: Exile the top card of target player's library. If it's a land card, you gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTopCardOfTargetPlayerLibraryEffect(1)),
                "{T}: Exile the top card of target player's library. If it's a land card, you gain 1 life."
        ));

        // Landfall — Whenever a land you control enters, you may untap this creature.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF),
                        "Untap Scrib Nibblers?"));
    }
}
