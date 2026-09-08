package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "124")
public class BreakingEntering extends Card {

    public BreakingEntering() {
        TargetFilter player = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");

        CardEffect breaking = new MillEffect(8, MillRecipient.TARGET_PLAYER);
        CardEffect entering = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .mandatory(true)
                .battlefieldEffectGrants(List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)))
                .battlefieldEffectGrantDuration(EffectDuration.UNTIL_END_OF_TURN)
                .build();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Breaking — Target player mills eight cards", breaking, player
                ).withManaCost("{U}{B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Entering — Put a creature card from a graveyard onto the battlefield under your control. It gains haste until end of turn",
                        entering
                ).withManaCost("{4}{B}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Breaking and then Entering",
                        List.of(breaking, entering),
                        player
                ).withManaCost("{4}{U}{B}{B}{R}")
        )));
    }
}
