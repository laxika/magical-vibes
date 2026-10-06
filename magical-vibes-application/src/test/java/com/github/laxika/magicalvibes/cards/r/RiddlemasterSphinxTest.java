package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiddlemasterSphinx.class, GreenwoodSentinel.class, Shock.class})
class RiddlemasterSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may return an opponent's creature to its owner's hand")
    void acceptingEtbBounceReturnsOpponentsCreature() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        UUID sentinelId = harness.getPermanentId(player2, "Greenwood Sentinel");
        castRiddlemasterSphinx();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sentinelId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInHand(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Declining the ETB bounce leaves the opponent's creature on the battlefield")
    void decliningEtbBounceLeavesCreature() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        castRiddlemasterSphinx();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Greenwood Sentinel"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("ETB cannot target a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        UUID sentinelId = harness.getPermanentId(player1, "Greenwood Sentinel");
        castRiddlemasterSphinx();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, sentinelId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Greenwood Sentinel"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInHand(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("With no opponent creatures the Sphinx enters without a target or may prompt")
    void entersWithoutLegalTargets() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        castRiddlemasterSphinx();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Riddlemaster Sphinx");
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature owned by the Sphinx controller returns to its owner, not its controller")
    void returnsCreatureToOwnerRatherThanController() {
        UUID sentinelId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();
        gd.stolenCreatures.put(sentinelId, player1.getId());
        castRiddlemasterSphinx();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sentinelId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInHand(player1, "Greenwood Sentinel");
        harness.assertNotInHand(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("The bounce trigger does not resolve when its target dies in response")
    void targetDiesBeforeTriggerResolves() {
        UUID sentinelId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();
        castRiddlemasterSphinx();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sentinelId);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, sentinelId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertNotInHand(player2, "Greenwood Sentinel");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castRiddlemasterSphinx() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RiddlemasterSphinx(), "{4}{U}{U}");
    }
}
