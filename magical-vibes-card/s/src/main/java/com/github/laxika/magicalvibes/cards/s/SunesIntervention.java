package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "29")
public class SunesIntervention extends Card {

    private static final CardPredicate NONLAND_PERMANENT_MANA_VALUE_THREE_OR_LESS = new CardAllOfPredicate(List.of(
            new CardIsPermanentPredicate(),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
            new CardMaxManaValuePredicate(3)));

    public SunesIntervention() {
        setAllowSharedTargets(true);

        var anyPlayerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 2/2 white Knight creature tokens",
                        new CreateTokenEffect(2, "Knight", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.KNIGHT), Set.of(), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Seek a nonland permanent card with mana value 3 or less",
                        new SeekLibraryEffect(1, NONLAND_PERMANENT_MANA_VALUE_THREE_OR_LESS)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.enchantment()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains 3 life",
                        new TargetPlayerGainsLifeEffect(3),
                        anyPlayerFilter)
        )));
    }
}
