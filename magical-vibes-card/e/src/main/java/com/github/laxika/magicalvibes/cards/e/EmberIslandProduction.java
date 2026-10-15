package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "48")
public class EmberIslandProduction extends Card {

    public EmberIslandProduction() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target creature you control, except it's not legendary and it's a 4/4 Hero in addition to its other types",
                        CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                                List.of(CardSubtype.HERO), Set.of(), 4, 4, Map.of()),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target creature an opponent controls, except it's not legendary and it's a 2/2 Coward in addition to its other types",
                        CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                                List.of(CardSubtype.COWARD), Set.of(), 2, 2, Map.of()),
                        TargetFilters.creatureAnOpponentControls())
        )));
    }
}
