package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanicTorrent.class, ChandraNalaar.class, GrizzlyBears.class, HillGiant.class,
        LightningBolt.class, Mountain.class})
class VolcanicTorrentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to opposing creatures and planeswalkers based on spells cast this turn")
    void damagesOpposingCreaturesAndPlaneswalkers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opposingPlaneswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        opposingPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt(), new VolcanicTorrent()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cascade casts a lesser spell and that spell increases the damage")
    void cascadeSpellCountsTowardDamage() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new VolcanicTorrent()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears")
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
