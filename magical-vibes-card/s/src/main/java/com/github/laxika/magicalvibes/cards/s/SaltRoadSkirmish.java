package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "88")
public class SaltRoadSkirmish extends Card {

    public SaltRoadSkirmish() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                        CardType.CREATURE, 2, "Warrior", 1, 1, CardColor.RED, null,
                        List.of(CardSubtype.WARRIOR), Set.of(), Set.of(), false, false,
                        Map.of(), List.of(), false, false, false, 0, Set.of(Keyword.HASTE)))
                .addEffect(EffectSlot.SPELL, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
