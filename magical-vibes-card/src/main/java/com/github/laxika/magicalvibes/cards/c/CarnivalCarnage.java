package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "222")
public class CarnivalCarnage extends Card {

    public CarnivalCarnage() {
        setAllowSharedTargets(true);

        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));
        TargetFilter creatureOrPlaneswalkerTarget = new PermanentPredicateTargetFilter(
                creatureOrPlaneswalker,
                "Target must be a creature or planeswalker.");
        TargetFilter opponentTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");

        CardEffect carnivalDamage = new DealDamageToTargetCreatureOrPlaneswalkerEffect(1);
        CardEffect carnivalControllerDamage = new DealDamageToPlayersEffect(
                1, DamageRecipient.TARGET_PERMANENT_CONTROLLER);
        CardEffect carnageDamage = new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER);
        CardEffect carnageDiscard = new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Carnival — Carnival deals 1 damage to target creature or planeswalker and 1 damage to that permanent's controller",
                        List.of(carnivalDamage, carnivalControllerDamage),
                        List.of(creatureOrPlaneswalkerTarget, creatureOrPlaneswalkerTarget)
                ).withManaCost("{B/R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Carnage — Carnage deals 3 damage to target opponent. That player discards two cards",
                        List.of(carnageDamage, carnageDiscard),
                        List.of(opponentTarget, opponentTarget)
                ).withManaCost("{2}{B}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Carnival and then Carnage",
                        List.of(carnivalDamage, carnivalControllerDamage, carnageDamage, carnageDiscard),
                        List.of(
                                creatureOrPlaneswalkerTarget, creatureOrPlaneswalkerTarget,
                                opponentTarget, opponentTarget)
                ).withManaCost("{2}{B}{R}{B/R}")
        )));
    }
}
