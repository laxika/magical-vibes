package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoseCutthroatRaider.class, Mountain.class})
class RoseCutthroatRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Raid creates one Junk for each opponent attacked")
    void raidCreatesJunkForEachOpponentAttacked() {
        addReadyRose();

        declareAttackers(java.util.List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Junk")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Junk adds red mana")
    void sacrificingJunkAddsRedMana() {
        addReadyRose();

        declareAttackers(java.util.List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent junk = findPermanent(player1, "Junk");
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(junk);
        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Raid creates no Junk when you did not attack")
    void raidCreatesNoJunkWithoutAttacking() {
        addReadyRose();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Junk")).isEmpty();
    }

    private Permanent addReadyRose() {
        return addCreatureReady(player1, new RoseCutthroatRaider());
    }

    @Test
    @DisplayName("Junk exiles only the top card and allows a land to be played")
    void junkAllowsPlayingExiledLand() {
        createJunk();
        Mountain top = new Mountain();
        Mountain next = new Mountain();
        harness.setLibrary(player1, List.of(top, next));

        activateJunk();
        resolveAllTriggers();

        assertThat(gd.playerExiledCards.get(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        assertThat(findPermanents(player1, "Junk")).isEmpty();
        harness.castFromExile(player1, top.getId());
        assertThat(findPermanents(player1, "Mountain")).hasSize(1);
        assertThat(gd.playerExiledCards.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Junk does not waive the exiled spell's mana cost")
    void junkRequiresNormalManaCost() {
        createJunk();
        RoseCutthroatRaider top = new RoseCutthroatRaider();
        harness.setLibrary(player1, List.of(top));

        activateJunk();
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("Junk can only be activated during a main phase")
    void junkCannotBeActivatedDuringCombat() {
        createJunk();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(this::activateJunk)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(findPermanents(player1, "Junk")).hasSize(1);
    }

    @Test
    @DisplayName("Junk play permission expires at the end of the turn")
    void junkPlayPermissionExpires() {
        createJunk();
        Mountain top = new Mountain();
        harness.setLibrary(player1, List.of(top, new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        activateJunk();
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("Sacrificing Junk does not trigger an opponent's Rose")
    void opponentsRoseDoesNotRewardYourSacrifice() {
        createJunk();
        harness.addToBattlefield(player2, new RoseCutthroatRaider());

        activateJunk();
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Sacrificing Junk with an empty library still adds red mana")
    void emptyLibraryStillRewardsJunkSacrifice() {
        createJunk();
        harness.setLibrary(player1, List.of());

        activateJunk();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Junk")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void createJunk() {
        addReadyRose();
        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateJunk() {
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Junk"));
        harness.activateAbility(player1, junkIndex, null, null);
    }
}
