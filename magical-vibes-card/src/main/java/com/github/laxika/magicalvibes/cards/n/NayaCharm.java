package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "180")
public class NayaCharm extends Card {

    public NayaCharm() {
        // Each mode carries its own intrinsic target channel: mode 0 targets a creature
        // (DealDamageToTargetCreatureEffect), mode 1 targets a graveyard card in any graveyard and
        // returns it to its owner's hand, mode 2 targets a player and taps all creatures they control.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Naya Charm deals 3 damage to target creature",
                        new DealDamageToTargetCreatureEffect(3),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target card from a graveyard to its owner's hand",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                                .targetGraveyard(true)
                                .build()),
                new ChooseOneEffect.ChooseOneOption(
                        "Tap all creatures target player controls",
                        new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS,
                                new PermanentIsCreaturePredicate()))
        )));
    }
}
