package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AbandonedOutpost;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.n.NimbleMongoose;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Embolden.class, AngelicWall.class, Firebolt.class, AbandonedOutpost.class, NimbleMongoose.class})
class EmboldenTest extends BaseCardTest {

    @Test
    void dividesPreventionAmongCreatureAndPlayer() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new AngelicWall());

        castEmbolden(Map.of(wall.getId(), 2, player2.getId(), 2));

        GameData gd = harness.getGameData();
        assertThat(wall.getDamagePreventionShield()).isEqualTo(2);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void appliesAllFourPreventionToSingleTarget() {
        castEmbolden(Map.of(player2.getId(), 4));

        GameData gd = harness.getGameData();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    void preventsDamageIndependentlyForEachTarget() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new AngelicWall());
        castEmbolden(Map.of(wall.getId(), 1, player2.getId(), 3));

        harness.setHand(player1, List.of(new Firebolt(), new Firebolt(), new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, wall.getId());
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(wall.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    void preventionShieldWearsOffAtEndOfTurn() {
        castEmbolden(Map.of(player2.getId(), 4));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    void assignmentsMustSumToFour() {
        prepareEmbolden();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AbandonedOutpost());
        prepareEmbolden();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(land.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashbackPreventsDamageAndExilesSpellAfterResolving() {
        harness.setGraveyard(player1, List.of(new Embolden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, Map.of(player2.getId(), 4));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Embolden");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Embolden"));
    }

    @Test
    void flashbackRejectsNoncreatureTargetBeforePayingMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AbandonedOutpost());
        harness.setGraveyard(player1, List.of(new Embolden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, Map.of(land.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Embolden");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseNoTargets() {
        castEmbolden(Map.of());

        harness.assertInGraveyard(player1, "Embolden");
        assertThat(gd.playerDamagePreventionShields).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackCanChooseNoTargets() {
        harness.setGraveyard(player1, List.of(new Embolden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, Map.of());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Embolden");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Embolden"));
        assertThat(gd.playerDamagePreventionShields).isEmpty();
    }

    @Test
    void cannotAssignZeroPreventionToATarget() {
        prepareEmbolden();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void separateSpellsProvideCumulativePrevention() {
        castEmbolden(Map.of(player2.getId(), 4));
        castEmbolden(Map.of(player2.getId(), 4));
        harness.setHand(player1, List.of(new Firebolt(), new Firebolt(), new Firebolt(),
                new Firebolt(), new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 5);

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());
        }
        harness.assertLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotCastTargetingCreatureWithShroud() {
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        prepareEmbolden();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(mongoose.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Embolden");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotFlashbackTargetingCreatureWithShroud() {
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        harness.setGraveyard(player1, List.of(new Embolden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, Map.of(mongoose.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Embolden");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareEmbolden() {
        harness.setHand(player1, List.of(new Embolden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castEmbolden(Map<java.util.UUID, Integer> assignments) {
        prepareEmbolden();
        harness.castInstant(player1, 0, assignments);
        harness.passBothPriorities();
    }
}
