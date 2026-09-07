package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "246")
public class CeaseDesist extends Card {

    public CeaseDesist() {
        TargetFilter anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");

        CardEffect exile = new ExileCardsFromGraveyardEffect(2, 0, true);
        CardEffect gainLife = new GainLifeEffect(new Fixed(2), GainLifeRecipient.TRIGGERING_PLAYER);
        CardEffect draw = new DrawCardForTargetPlayerEffect(1, false, true);
        List<CardEffect> cease = List.of(exile, gainLife, draw);
        CardEffect desist = new DestroyAllPermanentsEffect(new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate())));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Cease — Exile up to two target cards from a single graveyard. Target player gains 2 life and draws a card",
                        cease,
                        anyPlayer
                ).withManaCost("{1}{B/G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Desist — Destroy all artifacts and enchantments",
                        desist
                ).withManaCost("{4}{G/W}{G/W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Cease and then Desist",
                        List.of(exile, gainLife, draw, desist),
                        anyPlayer
                ).withManaCost("{5}{B/G}{G/W}{G/W}")
        )));
    }
}
