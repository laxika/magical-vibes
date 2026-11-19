package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromEachOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/** Flotsam // Jetsam, a split spell with one mode for each half. */
@CardRegistration(set = "MKM", collectorNumber = "247")
public class FlotsamJetsam extends Card {

    public FlotsamJetsam() {
        List<CardEffect> flotsam = List.of(
                new MillEffect(3, Subjects.you()),
                CreateTokenEffect.ofClueToken(1));
        List<CardEffect> jetsam = List.of(
                new MillEffect(3, Subjects.eachOpponent()),
                new CastSpellFromEachOpponentGraveyardEffect());

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Flotsam — Mill three cards. Investigate",
                        flotsam
                ).withManaCost("{1}{G/U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Jetsam — Each opponent mills three cards, then you may cast a spell from each opponent's graveyard without paying its mana cost",
                        jetsam
                ).withManaCost("{4}{U/B}{U/B}")
        )));
    }
}
