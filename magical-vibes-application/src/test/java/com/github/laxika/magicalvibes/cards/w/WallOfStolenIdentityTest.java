package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfStolenIdentity.class, GrizzlyBears.class, ControlMagic.class})
class WallOfStolenIdentityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a Wall copy with defender, taps the copied creature, and locks its untap")
    void copiesCreatureAndLocksIt() {
        Permanent bears = addBears();

        castWallAndChoose(bears.getId(), true);

        Permanent wall = copiedWall();
        assertThat(wall.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(wall.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.WALL);
        assertThat(wall.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(bears.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The copied creature untaps after Wall of Stolen Identity leaves")
    void copiedCreatureUntapsAfterWallLeaves() {
        Permanent bears = addBears();

        castWallAndChoose(bears.getId(), true);
        gd.playerBattlefields.get(player1.getId()).remove(copiedWall());

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void copiedCreatureUntapsAfterWallChangesControllers() {
        Permanent bears = addBears();
        castWallAndChoose(bears.getId(), true);
        assertThat(bears.isTapped()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, copiedWall().getId());
        resolveAllTriggers();
        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void copyChoiceAndReflexiveAbilityDoNotTarget() {
        Permanent bears = addBears();
        bears.getGrantedKeywords().add(Keyword.HEXPROOF);

        castWallAndChoose(bears.getId(), true);

        assertThat(bears.isTapped()).isTrue();
        assertThat(copiedWall()).isNotNull();
    }

    @Test
    @DisplayName("Declining the copy leaves the 0/0 Wall dead")
    void diesWhenCopyIsDeclined() {
        addBears();

        harness.setHand(player1, List.of(new WallOfStolenIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Wall of Stolen Identity");
        harness.assertInGraveyard(player1, "Wall of Stolen Identity");
    }

    private Permanent addBears() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        return gd.playerBattlefields.get(player2.getId()).getFirst();
    }

    private void castWallAndChoose(UUID targetId, boolean accept) {
        harness.setHand(player1, List.of(new WallOfStolenIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
        if (accept) {
            harness.handlePermanentChosen(player1, targetId);
            resolveAllTriggers();
        }
    }

    private Permanent copiedWall() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Wall of Stolen Identity"))
                .findFirst()
                .orElseThrow();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
