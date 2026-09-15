package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.h.Hoodwink;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Misdirection.class, Brainstorm.class, Hoodwink.class, Island.class, Counterspell.class,
        RishadanPort.class})
class MisdirectionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Misdirection requires targeting a spell with a single target")
    void castingRequiresSingleTargetSpell() {
        Brainstorm brainstorm = new Brainstorm();
        harness.castFromHand(player1, brainstorm, "{U}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Misdirection()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, brainstorm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Casting Misdirection cannot target a single-target ability")
    void cannotTargetSingleTargetAbility() {
        RishadanPort port = new RishadanPort();
        Island targetLand = new Island();
        harness.addToBattlefield(player1, port);
        harness.addToBattlefield(player2, targetLand);
        UUID targetLandPermanentId = harness.getPermanentId(player2, "Island");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, targetLandPermanentId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Misdirection()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, port.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell");
    }

    @Test
    @DisplayName("Misdirection retargets a single-target spell")
    void retargetsSpell() {
        Island island1 = new Island();
        Island island2 = new Island();
        harness.addToBattlefield(player1, island1);
        harness.addToBattlefield(player2, island2);
        UUID island1PermId = harness.getPermanentId(player1, "Island");
        UUID island2PermId = harness.getPermanentId(player2, "Island");

        Hoodwink hoodwink = new Hoodwink();
        harness.setHand(player1, List.of(hoodwink));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Misdirection()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, island1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hoodwink.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(island2PermId)
                .doesNotContain(island1PermId);

        harness.handlePermanentChosen(player2, island2PermId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(island2);
    }

    @Test
    @DisplayName("Misdirection may exile a blue card instead of paying mana")
    void alternateCostExilesBlueCard() {
        Island island1 = new Island();
        Island island2 = new Island();
        harness.addToBattlefield(player1, island1);
        harness.addToBattlefield(player2, island2);
        UUID island1PermId = harness.getPermanentId(player1, "Island");
        UUID island2PermId = harness.getPermanentId(player2, "Island");

        Hoodwink hoodwink = new Hoodwink();
        harness.setHand(player1, List.of(hoodwink));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Counterspell blueCard = new Counterspell();
        harness.setHand(player2, List.of(new Misdirection(), blueCard));
        harness.castInstant(player1, 0, island1PermId);
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, hoodwink.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, island2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(island2);
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Counterspell");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Misdirection's alternate cost requires exiling a blue card")
    void alternateCostRequiresBlueCard() {
        Island target = new Island();
        harness.addToBattlefield(player1, target);
        UUID targetPermanentId = harness.getPermanentId(player1, "Island");

        Hoodwink hoodwink = new Hoodwink();
        harness.setHand(player1, List.of(hoodwink));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetPermanentId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Misdirection(), new Island()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player2, 0, hoodwink.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue card");
    }
}
