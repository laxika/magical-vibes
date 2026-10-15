package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayExiledCostCardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "123")
@CardRegistration(set = "C18", collectorNumber = "12")
public class PrimordialMist extends Card {

    public PrimordialMist() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayEffect(new ManifestTopCardEffect(), "Manifest the top card of your library?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExilePermanentCost(
                                new PermanentIsFaceDownPredicate(),
                                "a face-down permanent",
                                true,
                                false,
                                false,
                                true),
                        new AllowPlayExiledCostCardThisTurnEffect()),
                "Exile a face-down permanent you control face up: You may play that card this turn."
        ));
    }
}
