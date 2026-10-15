package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "182")
public class VerdantCommand extends Card {

    public VerdantCommand() {
        setAllowSharedTargets(true);

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var loyaltyAbility = new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)),
                        new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)))),
                "Target must be a loyalty ability of a planeswalker.");
        var squirrels = new CreateTokenEffect(
                2, "Squirrel", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()).withTapped(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player creates two tapped 1/1 green Squirrel creature tokens",
                        new CreateTokenForTargetPlayerEffect(squirrels), anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target loyalty ability of a planeswalker",
                        new CounterSpellEffect(), loyaltyAbility),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target card from a graveyard",
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false)),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains 3 life",
                        new TargetPlayerGainsLifeEffect(3), anyPlayer)
        ), 2));
    }
}
