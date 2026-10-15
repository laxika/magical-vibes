package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealityStrobe.class, BlindPhantasm.class, HorizonCanopy.class})
class RealityStrobeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent and exiles Reality Strobe with three time counters")
    void returnsTargetPermanentAndExilesWithSuspendCounters() {
        RealityStrobe strobe = new RealityStrobe();
        harness.addToBattlefield(player2, new BlindPhantasm());
        harness.setHand(player1, List.of(strobe));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Blind Phantasm"));

        harness.assertNotOnBattlefield(player2, "Blind Phantasm");
        harness.assertInHand(player2, "Blind Phantasm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(strobe);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(strobe.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Suspend exiles Reality Strobe with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        RealityStrobe strobe = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(strobe);
        assertThat(gd.exiledCardTimeCounters).containsEntry(strobe.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A suspended Reality Strobe free-casts, returns its target, and starts a new countdown")
    void suspendedCardFreeCastsAndExilesAgain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        RealityStrobe strobe = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Blind Phantasm");
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(strobe.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Reality Strobe exiled")
    void decliningSuspendCastLeavesCardExiled() {
        RealityStrobe strobe = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(strobe);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(strobe.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Reality Strobe"));
    }

    @Test
    @DisplayName("Reality Strobe can return a land controlled by its caster")
    void returnsOwnLand() {
        RealityStrobe strobe = new RealityStrobe();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HorizonCanopy());
        harness.setHand(player1, List.of(strobe));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        harness.assertNotOnBattlefield(player1, "Horizon Canopy");
        harness.assertInHand(player1, "Horizon Canopy");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(strobe);
        harness.assertNotInGraveyard(player1, "Reality Strobe");
    }

    @Test
    @DisplayName("An illegal target prevents Reality Strobe from exiling itself")
    void sacrificedTargetMakesSpellGoToGraveyard() {
        RealityStrobe strobe = new RealityStrobe();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new HorizonCanopy());
        harness.setLibrary(player2, List.of(new BlindPhantasm()));
        harness.setHand(player1, List.of(strobe));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, land.getId());
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reality Strobe");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(strobe);
        assertThat(gd.suspendedSpellExiles).isEmpty();
        harness.assertNotInHand(player2, "Horizon Canopy");
    }

    @Test
    @DisplayName("A resolved Reality Strobe loses counters only during its owner's upkeep")
    void resolvedSpellCountdownUsesOwnersUpkeep() {
        RealityStrobe strobe = new RealityStrobe();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        harness.setHand(player1, List.of(strobe));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        advanceToUpkeep(player2);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(strobe.getId(), player1.getId(), 3));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(strobe.getId(), player1.getId(), 2));

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(strobe);
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    private RealityStrobe suspendCard() {
        RealityStrobe strobe = new RealityStrobe();
        harness.setHand(player1, List.of(strobe));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);
        return strobe;
    }
}
