package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
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

@CardUsed({RangersHawk.class, NeverwinterDryad.class})
class RangersHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the Hawk and another creature ventures into the dungeon")
    void tapsSourceAndAnotherCreatureToVenture() {
        Permanent hawk = addCreatureReady(player1, new RangersHawk());
        Permanent otherCreature = addCreatureReady(player1, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(hawk.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("The ability cannot be activated without another untapped creature")
    void requiresAnotherUntappedCreature() {
        addCreatureReady(player1, new RangersHawk());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    @DisplayName("The ability cannot be activated outside its controller's main phase")
    void requiresSorceryTiming() {
        addCreatureReady(player1, new RangersHawk());
        addCreatureReady(player1, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    void canTapAnotherCreatureWithSummoningSickness() {
        Permanent hawk = addCreatureReady(player1, new RangersHawk());
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        dryad.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(hawk.isTapped()).isTrue();
        assertThat(dryad.isTapped()).isTrue();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
    }

    @Test
    void cannotActivateWhileHawkHasSummoningSickness() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new RangersHawk());
        hawk.setSummoningSick(true);
        Permanent dryad = addCreatureReady(player1, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(hawk.isTapped()).isFalse();
        assertThat(dryad.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapAnAlreadyTappedCreatureForTheAdditionalCost() {
        addCreatureReady(player1, new RangersHawk());
        Permanent dryad = addCreatureReady(player1, new NeverwinterDryad());
        dryad.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapAnOpponentsCreatureForTheAdditionalCost() {
        addCreatureReady(player1, new RangersHawk());
        Permanent opponentCreature = addCreatureReady(player2, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileHawkIsTapped() {
        Permanent hawk = addCreatureReady(player1, new RangersHawk());
        hawk.tap();
        Permanent dryad = addCreatureReady(player1, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(dryad.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent hawk = addCreatureReady(player1, new RangersHawk());
        Permanent dryad = addCreatureReady(player1, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(hawk.isTapped()).isFalse();
        assertThat(dryad.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesWhichOtherCreatureToTapWhenSeveralAreAvailable() {
        Permanent hawk = addCreatureReady(player1, new RangersHawk());
        Permanent chosen = addCreatureReady(player1, new NeverwinterDryad());
        Permanent other = addCreatureReady(player1, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(hawk.isTapped()).isTrue();
        assertThat(chosen.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Tomb of Annihilation");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
    }

    @Test
    void requiresThreeMana() {
        Permanent hawk = addCreatureReady(player1, new RangersHawk());
        Permanent dryad = addCreatureReady(player1, new NeverwinterDryad());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hawk.isTapped()).isFalse();
        assertThat(dryad.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithASpellOnTheStack() {
        Permanent hawk = addCreatureReady(player1, new RangersHawk());
        Permanent dryad = addCreatureReady(player1, new NeverwinterDryad());
        harness.setHand(player1, List.of(new NeverwinterDryad()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(hawk.isTapped()).isFalse();
        assertThat(dryad.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void advancesInTheExistingDungeonInsteadOfStartingAnother() {
        addCreatureReady(player1, new RangersHawk());
        addCreatureReady(player1, new NeverwinterDryad());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Goblin Lair");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));
    }
}
