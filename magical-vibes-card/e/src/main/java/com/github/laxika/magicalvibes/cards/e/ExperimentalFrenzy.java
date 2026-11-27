package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "99")
public class ExperimentalFrenzy extends Card {

    public ExperimentalFrenzy() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new PlayLandsFromTopOfLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(Set.of(
                CardType.CREATURE,
                CardType.ENCHANTMENT,
                CardType.SORCERY,
                CardType.INSTANT,
                CardType.ARTIFACT,
                CardType.PLANESWALKER,
                CardType.BATTLE,
                CardType.KINDRED)));
        addStaticEffect(new PlayersCantCastSpellsFromZonesEffect(Subjects.you(),
                PlayerRestriction.playingCardsFrom(Zone.HAND).ofTheirOwn()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new DestroyTargetPermanentEffect(Subjects.source())),
                "{3}{R}: Destroy Experimental Frenzy."
        ));
    }
}
