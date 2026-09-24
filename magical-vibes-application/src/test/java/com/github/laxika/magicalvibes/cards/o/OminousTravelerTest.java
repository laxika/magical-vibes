package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArchghoulOfThraben;
import com.github.laxika.magicalvibes.cards.b.BladestitchedSkaab;
import com.github.laxika.magicalvibes.cards.c.ChampionOfThePerished;
import com.github.laxika.magicalvibes.cards.d.DominatingVampire;
import com.github.laxika.magicalvibes.cards.f.FalkenrathPitFighter;
import com.github.laxika.magicalvibes.cards.h.HeadlessRider;
import com.github.laxika.magicalvibes.cards.h.HowlpackPiper;
import com.github.laxika.magicalvibes.cards.h.HeronBlessedGeist;
import com.github.laxika.magicalvibes.cards.p.PatricianGeist;
import com.github.laxika.magicalvibes.cards.s.ShipwreckSifters;
import com.github.laxika.magicalvibes.cards.s.SteelcladSpirit;
import com.github.laxika.magicalvibes.cards.s.StromkirkBloodthief;
import com.github.laxika.magicalvibes.cards.t.TovolarDireOverlord;
import com.github.laxika.magicalvibes.cards.v.VampireSocialite;
import com.github.laxika.magicalvibes.cards.w.WolfkinOutcast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OminousTraveler.class, DominatingVampire.class, VampireSocialite.class,
        StromkirkBloodthief.class, FalkenrathPitFighter.class, WolfkinOutcast.class,
        HowlpackPiper.class, TovolarDireOverlord.class, PatricianGeist.class,
        ShipwreckSifters.class, SteelcladSpirit.class, HeronBlessedGeist.class,
        ArchghoulOfThraben.class, ChampionOfThePerished.class, HeadlessRider.class,
        BladestitchedSkaab.class})
class OminousTravelerTest extends BaseCardTest {

    @Test
    void entersAndOffersThreeSpellbookChoices() {
        harness.enterBattlefieldAndReturn(player1, new OminousTraveler());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }

    @Test
    void draftedCardCanUseAnyColorAndReturnsAnOminousTravelerWhenCast() {
        Permanent traveler = harness.enterBattlefieldAndReturn(player1, new OminousTraveler());
        resolveAllTriggers();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        int draftedIndex = gd.playerHands.get(player1.getId()).indexOf(drafted);
        harness.castCreature(player1, draftedIndex);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, traveler.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, drafted.getName());
        harness.assertInHand(player1, "Ominous Traveler");
    }
}
