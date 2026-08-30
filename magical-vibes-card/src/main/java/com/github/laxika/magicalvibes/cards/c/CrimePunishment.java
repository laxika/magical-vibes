package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "150")
public class CrimePunishment extends Card {

    public CrimePunishment() {
        CardPredicate creatureOrEnchantmentCard = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.ENCHANTMENT)));
        PermanentPredicate artifactCreatureOrEnchantment = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentManaValueEqualsXPredicate()));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Crime — Put target creature or enchantment card from an opponent's graveyard onto the battlefield under your control",
                        new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                                false, creatureOrEnchantmentCard, false),
                        new GraveyardCardPredicateTargetFilter(
                                creatureOrEnchantmentCard, GraveyardSearchScope.OPPONENT_GRAVEYARD)
                ).withManaCost("{3}{W}{B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Punishment — Destroy each artifact, creature, and enchantment with mana value X",
                        new DestroyAllPermanentsEffect(artifactCreatureOrEnchantment)
                ).withManaCost("{X}{B}{G}")
        )));
    }
}
