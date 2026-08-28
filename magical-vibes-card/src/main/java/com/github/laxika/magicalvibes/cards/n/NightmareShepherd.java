package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "108")
public class NightmareShepherd extends Card {

    public NightmareShepherd() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new MayEffect(
                new ExileDyingCreatureAndCreateTokenCopyEffect(
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(CardSubtype.NIGHTMARE), Set.of(), 1, 1, Map.of())),
                "Exile that creature to create a 1/1 Nightmare token copy?"));
    }
}
