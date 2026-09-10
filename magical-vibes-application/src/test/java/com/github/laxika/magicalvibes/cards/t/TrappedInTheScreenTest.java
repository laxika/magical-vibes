package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrappedInTheScreen.class, GrizzlyBears.class, Forest.class, GloriousAnthem.class,
        LeoninScimitar.class, Naturalize.class})
class TrappedInTheScreenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opponent's creature until Trapped in the Screen leaves")
    void exilesCreatureUntilSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndExileTarget(creatureId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThatExiled(player2, "Grizzly Bears");

        resetForFollowUpSpell();
        destroySourceWithNaturalize();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Trapped in the Screen");
    }

    @Test
    @DisplayName("ETB exiles an opponent's artifact")
    void exilesArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndExileTarget(artifactId);

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThatExiled(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("ETB exiles an opponent's enchantment")
    void exilesEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID enchantmentId = harness.getPermanentId(player2, "Glorious Anthem");
        castAndExileTarget(enchantmentId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThatExiled(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a land or a permanent controlled by this card's controller")
    void rejectsIllegalTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID landId = harness.getPermanentId(player2, "Forest");
        prepareToCast();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreatureId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay {2}")
    void wardCountersUnpaidSpell() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TrappedInTheScreen());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Naturalize");
        harness.assertOnBattlefield(player1, "Trapped in the Screen");
    }

    private void castAndExileTarget(UUID targetId) {
        prepareToCast();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    private void prepareToCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TrappedInTheScreen()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void destroySourceWithNaturalize() {
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        UUID sourceId = harness.getPermanentId(player1, "Trapped in the Screen");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sourceId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
    }

    private void assertThatExiled(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        org.assertj.core.api.Assertions.assertThat(gd.getPlayerExiledCards(player.getId()))
                .anyMatch(card -> card.getName().equals(cardName));
    }
}
