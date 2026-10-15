package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EssenceSymbiote;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WhisperSquad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DireTactics.class, WhisperSquad.class, Forest.class, EssenceSymbiote.class})
class DireTacticsTest extends BaseCardTest {

    private void castDireTactics(Permanent target) {
        prepareDireTactics();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareDireTactics() {
        harness.setHand(player1, List.of(new DireTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Exiles a creature and loses life equal to its toughness")
    void exilesCreatureAndLosesLife() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());

        castDireTactics(target);

        harness.assertNotOnBattlefield(player2, "Essence Symbiote");
        harness.assertNotInGraveyard(player2, "Essence Symbiote");
        assertThat(harness.getGameData().exiledCards).anyMatch(
                exiled -> exiled.card().getName().equals("Essence Symbiote"));
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Uses the creature's effective toughness")
    void usesEffectiveToughness() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        target.setToughnessModifier(3);

        castDireTactics(target);

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does not lose life while controlling a Human")
    void noLifeLossWhileControllingHuman() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new WhisperSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());

        castDireTactics(target);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Exiling the only Human causes the life loss")
    void exilingOnlyHumanCausesLifeLoss() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WhisperSquad());

        castDireTactics(target);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareDireTactics();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Human does not prevent life loss")
    void opponentsHumanDoesNotPreventLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new WhisperSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());

        castDireTactics(target);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Whisper Squad");
    }

    @Test
    @DisplayName("Exiling a Human does not cause life loss if another Human remains")
    void anotherHumanStillPreventsLifeLoss() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WhisperSquad());
        Permanent remainingHuman = harness.addToBattlefieldAndReturn(player1, new WhisperSquad());

        castDireTactics(target);

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(remainingHuman.getId()).doesNotContain(target.getId());
    }

    @Test
    @DisplayName("A Human entering before resolution prevents life loss")
    void humanEnteringBeforeResolutionPreventsLifeLoss() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        prepareDireTactics();
        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new WhisperSquad());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Essence Symbiote");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Losing the only Human before resolution causes life loss")
    void humanLeavingBeforeResolutionCausesLifeLoss() {
        harness.setLife(player1, 20);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new WhisperSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        prepareDireTactics();
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, human);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Essence Symbiote");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An absent target prevents both exile and life loss")
    void absentTargetPreventsLifeLoss() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EssenceSymbiote());
        prepareDireTactics();
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
