package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "68")
public class MuYanlingSkyDancer extends Card {

    private static final String EMBLEM_TEXT = "Islands you control have '{T}: Draw a card.'";

    public MuYanlingSkyDancer() {
        // +2: Up to one target creature gets -2/-0 and loses flying until your next turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new BoostTargetCreatureEffect(-2, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN),
                        new RemoveKeywordEffect(Keyword.FLYING, GrantScope.TARGET,
                                EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "+2: Until your next turn, up to one target creature gets -2/-0 and loses flying.",
                null, +2, null, null,
                List.<TargetFilter>of(TargetFilters.creature()), 0, 1));

        // -3: Create a 4/4 blue Elemental Bird creature token with flying.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new CreateTokenEffect("Elemental Bird", 4, 4, CardColor.BLUE,
                        List.of(CardSubtype.ELEMENTAL, CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())),
                "-3: Create a 4/4 blue Elemental Bird creature token with flying."));

        // -8: You get an emblem with "Islands you control have '{T}: Draw a card.'"
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        true, null,
                                        List.of(new DrawCardEffect(1)),
                                        "{T}: Draw a card."),
                                GrantScope.OWN_PERMANENTS,
                                new PermanentHasSubtypePredicate(CardSubtype.ISLAND))),
                        EMBLEM_TEXT)),
                "-8: You get an emblem with \"" + EMBLEM_TEXT + "\""));
    }
}
