package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "96")
public class KuldothaRebirth extends Card {

    public KuldothaRebirth() {
        addEffect(EffectSlot.SPELL, new SacrificeArtifactCost());
        addEffect(EffectSlot.SPELL, new CreateCreatureTokenEffect(
                3, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
