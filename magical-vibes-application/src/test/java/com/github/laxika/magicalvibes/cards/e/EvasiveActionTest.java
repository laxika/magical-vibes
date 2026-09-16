package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvasiveAction.class, Forest.class, GaeasSkyfolk.class, Island.class, Mountain.class,
        Plains.class, Swamp.class})
class EvasiveActionTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay for the domain count")
    void countersWhenControllerCannotPayDomainCost() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());

        castEvasiveAction(4);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gaea's Skyfolk");
        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The spell resolves when its controller pays one for each basic land type")
    void resolvesWhenControllerPaysDomainCost() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());

        castEvasiveAction(5);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("Counts distinct basic land types rather than lands")
    void countsDistinctBasicLandTypes() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castEvasiveAction(3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("Evaluates all five basic land types at resolution")
    void evaluatesAllFiveBasicLandTypesAtResolution() {
        castEvasiveAction(7);

        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        harness.setHand(player2, List.of(new EvasiveAction()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getPermanentId(player1, "Gaea's Skyfolk")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
    }

    private void castEvasiveAction(int playerMana) {
        GaeasSkyfolk skyfolk = new GaeasSkyfolk();
        harness.castFromHand(player1, skyfolk, "{G}{U}");
        harness.addMana(player1, ManaColor.GREEN, playerMana - 2);

        harness.setHand(player2, List.of(new EvasiveAction()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, skyfolk.getId());
    }
}
