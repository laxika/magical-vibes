package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "7")
public class BlindingBeam extends Card {

    public BlindingBeam() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{1}"));

        var playerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap two target creatures",
                        List.<CardEffect>of(new TapPermanentsEffect(
                                TapUntapScope.TARGET, new PermanentIsCreaturePredicate())),
                        TargetFilters.creature(), null, 2, 2, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures don't untap during target player's next untap step",
                        new SkipNextUntapEffect(
                                TapUntapScope.TARGET_PLAYERS_PERMANENTS,
                                new PermanentIsCreaturePredicate()),
                        playerFilter)
        )));
    }
}
