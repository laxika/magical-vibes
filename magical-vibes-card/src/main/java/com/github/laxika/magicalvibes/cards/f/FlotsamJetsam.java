package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromEachOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import java.util.List;

/** Flotsam // Jetsam, a split spell with one mode for each half. */
@CardRegistration(set = "MKM", collectorNumber = "247")
public class FlotsamJetsam extends Card {

    public FlotsamJetsam() {
        List<CardEffect> flotsam = List.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                CreateTokenEffect.ofClueToken(1));
        List<CardEffect> jetsam = List.of(
                new MillEffect(3, MillRecipient.EACH_OPPONENT),
                new CastSpellFromEachOpponentGraveyardEffect());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
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
