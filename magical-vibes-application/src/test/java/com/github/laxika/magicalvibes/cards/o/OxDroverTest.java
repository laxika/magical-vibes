package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OxDrover.class, GrizzlyBears.class, YokedOx.class})
class OxDroverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the targeted opponent an Ox and draws a card")
    void etbCreatesOxAndDraws() {
        harness.setHand(player1, List.of(new OxDrover()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addOxDroverMana();

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertOxToken(player2);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking gives the chosen opponent an Ox and draws a card")
    void attackCreatesOxAndDraws() {
        addCreatureReady(player1, new OxDrover());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertOxToken(player2);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger cannot target its controller")
    void triggerRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new OxDrover()));
        addOxDroverMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ox Drover cannot be blocked by an Ox")
    void cannotBeBlockedByOx() {
        addCreatureReady(player1, new OxDrover()).setAttacking(true);
        addCreatureReady(player2, new YokedOx());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ox Drover can be blocked by a non-Ox creature")
    void canBeBlockedByNonOx() {
        Permanent attacker = addCreatureReady(player1, new OxDrover());
        attacker.setAttacking(true);
        Permanent nonOxBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(nonOxBlocker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0)));

        assertThat(nonOxBlocker.isBlocking()).isTrue();
    }

    private void addOxDroverMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void assertOxToken(com.github.laxika.magicalvibes.model.Player player) {
        List<Permanent> oxen = findPermanents(player, "Ox").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(oxen).hasSize(1);
        Permanent ox = oxen.getFirst();
        assertThat(ox.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(ox.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ox.getCard().getSubtypes()).containsExactly(CardSubtype.OX);
        assertThat(ox.getCard().getPower()).isEqualTo(2);
        assertThat(ox.getCard().getToughness()).isEqualTo(4);
    }
}
