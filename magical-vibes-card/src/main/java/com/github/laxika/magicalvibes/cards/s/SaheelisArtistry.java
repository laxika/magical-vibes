package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "62")
public class SaheelisArtistry extends Card {

    public SaheelisArtistry() {
        setAllowSharedTargets(true);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target artifact",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target creature, except it's an artifact in addition to its other types",
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(), Set.of(CardType.ARTIFACT), null, null, Map.of()),
                        TargetFilters.creature())
        )));
    }
}
