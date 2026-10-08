package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWondrousWasp.class, AirElemental.class, MindStone.class, Unsummon.class})
class TheWondrousWaspTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps a target creature and removes its abilities while the Wasp remains")
    void etbTapsAndRemovesAbilities() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castWasp(elemental.getId());
        resolveAllTriggers();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, elemental)).isTrue();
    }

    @Test
    @DisplayName("The target regains its abilities when The Wondrous Wasp leaves")
    void abilitiesReturnWhenWaspLeaves() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castWasp(elemental.getId());
        resolveAllTriggers();

        Permanent wasp = findPermanent(player1, "The Wondrous Wasp");
        bounceWasp(wasp.getId());

        assertThat(gqs.hasLostAllAbilities(gd, elemental)).isFalse();
    }

    @Test
    @DisplayName("If the Wasp leaves before its ETB resolves, the creature is tapped but keeps its abilities")
    void sourceLeavesBeforeEtbResolution() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castWasp(elemental.getId());
        harness.passBothPriorities();

        Permanent wasp = findPermanent(player1, "The Wondrous Wasp");
        bounceWasp(wasp.getId());
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, elemental)).isFalse();
    }

    @Test
    @DisplayName("The ETB trigger may resolve without a target")
    void etbMayHaveNoTarget() {
        castWasp(null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Wondrous Wasp");
    }

    @Test
    @DisplayName("A creature you control may be targeted")
    void mayTargetOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        castWasp(elemental.getId());
        resolveAllTriggers();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, elemental)).isTrue();
    }

    @Test
    @DisplayName("The ETB trigger cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());

        harness.setHand(player1, List.of(new TheWondrousWasp()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, mindStone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already tapped target still loses its abilities")
    void alreadyTappedTargetLosesAbilities() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        elemental.tap();

        castWasp(elemental.getId());
        resolveAllTriggers();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Untapping the target does not end its ability loss")
    void untappingDoesNotRestoreAbilities() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castWasp(elemental.getId());
        resolveAllTriggers();
        harness.performUntapStep(player2);

        assertThat(elemental.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A target that leaves before resolution is not affected when it returns")
    void departedTargetIsNotAffectedOnReturn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castWasp(elemental.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Air Elemental");
        assertThat(returned.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Wasp can enter during an opponent's combat and tap a creature")
    void flashDuringOpponentsCombat() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.ensurePriority(player1);

        castWasp(elemental.getId());
        resolveAllTriggers();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        harness.assertOnBattlefield(player1, "The Wondrous Wasp");
    }

    @Test
    @DisplayName("Declining a target leaves an available creature unaffected")
    void mayDeclineWithCreatureAvailable() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castWasp(null);
        resolveAllTriggers();

        assertThat(elemental.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Returning the Wasp does not restart its previous ability-loss effect")
    void returningSourceDoesNotRestartEffect() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castWasp(elemental.getId());
        resolveAllTriggers();
        bounceWasp(findPermanent(player1, "The Wondrous Wasp").getId());

        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        harness.assertOnBattlefield(player1, "The Wondrous Wasp");
    }

    private void castWasp(UUID targetId) {
        harness.setHand(player1, List.of(new TheWondrousWasp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        if (targetId == null) {
            harness.castCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0, 0, targetId);
        }
    }

    private void bounceWasp(UUID waspId) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, waspId);
    }
}
