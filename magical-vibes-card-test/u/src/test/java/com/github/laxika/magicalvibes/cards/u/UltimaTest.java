package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ultima.class, DarksteelRelic.class, DarksteelMyr.class, FesteringGoblin.class,
        Forest.class, GrizzlyBears.class, HowlingMine.class, Ornithopter.class})
class UltimaTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts and creatures before ending the turn")
    void destroysArtifactsAndCreaturesThenEndsTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new HowlingMine());

        int turnBefore = gd.turnNumber;
        harness.castFromHand(player1, new Ultima(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ultima"));
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
    }

    @Test
    @DisplayName("Indestructible artifacts survive")
    void indestructibleArtifactsSurvive() {
        harness.addToBattlefield(player2, new DarksteelRelic());
        harness.castFromHand(player1, new Ultima(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Relic");
    }

    @Test
    @DisplayName("Targeted death triggers caused by the destruction cease to exist")
    void discardsTargetedDeathTriggers() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new DarksteelMyr());

        harness.castFromHand(player1, new Ultima(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Festering Goblin");
        harness.assertOnBattlefield(player2, "Darksteel Myr");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Ultima is exiled before cleanup discards and remains exiled afterward")
    void exilesUltimaBeforeCleanupDiscards() {
        Ultima ultima = new Ultima();
        harness.setHand(player1, List.of(ultima,
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultima);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultima);
        harness.assertNotInGraveyard(player1, "Ultima");
    }

    @Test
    @DisplayName("Cleanup discards precede removing marked damage and temporary modifiers")
    void discardsBeforeRemovingDamageAndTemporaryModifiers() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        myr.setMarkedDamage(1);
        myr.setPowerModifier(3);
        myr.setToughnessModifier(3);
        harness.setHand(player1, List.of(new Ultima(),
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(myr.getMarkedDamage()).isEqualTo(1);
        assertThat(myr.getPowerModifier()).isEqualTo(3);
        assertThat(myr.getToughnessModifier()).isEqualTo(3);

        harness.handleCardChosen(player1, 0);

        assertThat(myr.getMarkedDamage()).isZero();
        assertThat(myr.getPowerModifier()).isZero();
        assertThat(myr.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Darksteel Myr");
    }
}
