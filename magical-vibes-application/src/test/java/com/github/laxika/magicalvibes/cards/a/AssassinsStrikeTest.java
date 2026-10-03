package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.l.LoxodonSmiter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssassinsStrike.class, AxebaneStag.class, Cancel.class, Forest.class, LoxodonSmiter.class})
class AssassinsStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature is destroyed and its controller discards a card")
    void destroysCreatureAndControllerDiscards() {
        harness.addToBattlefield(player2, new AxebaneStag());
        UUID target = harness.getPermanentId(player2, "Axebane Stag");
        harness.setHand(player2, new ArrayList<>(List.of(new Cancel(), new Forest())));
        cast(target);

        harness.assertNotOnBattlefield(player2, "Axebane Stag");
        harness.assertInGraveyard(player2, "Axebane Stag");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player2, "Axebane Stag");
    }

    @Test
    @DisplayName("Empty-handed controller discards nothing but the creature is still destroyed")
    void emptyHandStillDestroys() {
        harness.addToBattlefield(player2, new AxebaneStag());
        UUID target = harness.getPermanentId(player2, "Axebane Stag");
        harness.setHand(player2, new ArrayList<>(List.of()));
        cast(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Axebane Stag");
    }

    @Test
    @DisplayName("Your own creature can be targeted, making you discard")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new AxebaneStag());
        UUID target = harness.getPermanentId(player1, "Axebane Stag");
        harness.setHand(player1, new ArrayList<>(List.of(new AssassinsStrike(), new Cancel())));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player1, 0, List.of(target));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Axebane Stag");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID land = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new AssassinsStrike()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal target prevents both destruction and discard")
    void targetGoneBeforeResolutionDoesNotDiscard() {
        harness.addToBattlefield(player2, new AxebaneStag());
        UUID target = harness.getPermanentId(player2, "Axebane Stag");
        harness.setHand(player2, List.of(new Cancel(), new Forest()));
        harness.setHand(player1, List.of(new AssassinsStrike()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, List.of(target));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Assassin's Strike");
    }

    @Test
    @DisplayName("Discard from your own spell does not put Loxodon Smiter onto the battlefield")
    void ownSpellDoesNotEnableOpponentDiscardReplacement() {
        harness.addToBattlefield(player1, new AxebaneStag());
        UUID target = harness.getPermanentId(player1, "Axebane Stag");
        harness.setHand(player1, List.of(new AssassinsStrike(), new LoxodonSmiter(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player1, 0, List.of(target));
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Loxodon Smiter");
        harness.assertNotOnBattlefield(player1, "Loxodon Smiter");
        harness.assertInGraveyard(player1, "Axebane Stag");
    }

    @Test
    @DisplayName("An opponent's spell puts discarded Loxodon Smiter onto the battlefield")
    void opponentSpellEnablesDiscardReplacement() {
        harness.addToBattlefield(player2, new AxebaneStag());
        UUID target = harness.getPermanentId(player2, "Axebane Stag");
        harness.setHand(player2, List.of(new LoxodonSmiter(), new Forest()));
        cast(target);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
        harness.assertInGraveyard(player2, "Axebane Stag");
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new AssassinsStrike()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));
    }
}
