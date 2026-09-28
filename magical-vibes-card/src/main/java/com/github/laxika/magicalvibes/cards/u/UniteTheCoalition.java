package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "15")
@CardRegistration(set = "DMC", collectorNumber = "91")
public class UniteTheCoalition extends Card {

    public UniteTheCoalition() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");
        var artifactOrEnchantment = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                "Target must be an artifact or enchantment");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Target permanent phases out",
                        () -> new PhaseOutEffect(PhaseOutSubject.TARGET),
                        TargetFilters.permanent()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Target player draws a card",
                        () -> new DrawCardForTargetPlayerEffect(1, false, true),
                        anyPlayer),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Exile target player's graveyard",
                        () -> new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE),
                        anyPlayer),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Unite the Coalition deals 2 damage to any target",
                        () -> new DealDamageToAnyTargetEffect(2),
                        null),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Destroy target artifact or enchantment",
                        () -> new DestroyTargetPermanentEffect(artifactOrEnchantment.predicate()),
                        artifactOrEnchantment)
        ), 5));
    }
}
