package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesOneOfEachTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/** Catch // Release, a split spell with three casting modes. */
@CardRegistration(set = "DGM", collectorNumber = "125")
public class CatchRelease extends Card {

    public CatchRelease() {
        TargetFilter permanent = TargetFilters.permanent();
        CardEffect catchHalf = SequenceEffect.of(
                new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN),
                new UntapPermanentsEffect(Subjects.target()),
                new GrantKeywordEffect(Keyword.HASTE, Subjects.target()));
        CardEffect releaseHalf = new EachPlayerSacrificesOneOfEachTypeEffect();

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Catch — Gain control of target permanent until end of turn. Untap it. It gains haste until end of turn",
                        catchHalf,
                        permanent
                ).withManaCost("{1}{U}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Release — Each player sacrifices an artifact, a creature, an enchantment, a land, and a planeswalker",
                        releaseHalf
                ).withManaCost("{4}{R}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Catch and then Release",
                        List.of(catchHalf, releaseHalf),
                        List.of(permanent)
                ).withManaCost("{5}{R}{R}{U}{W}").onlyFromHand()
        )));
    }
}
