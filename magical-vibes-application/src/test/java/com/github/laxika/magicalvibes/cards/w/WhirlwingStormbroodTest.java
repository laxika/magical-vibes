package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RunescaleStormbrood;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({WhirlwingStormbrood.class, Divination.class, Forest.class, GrizzlyBears.class,
        RunescaleStormbrood.class})
class WhirlwingStormbroodTest extends BaseCardTest {

    @Test
    @DisplayName("Whirlwing Stormbrood can be cast as a creature")
    void canCastCreatureFace() {
        harness.setHand(player1, List.of(new WhirlwingStormbrood()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Dynamic Soar puts three +1/+1 counters on a creature you control and shuffles")
    void omenPutsCountersAndShuffles() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        WhirlwingStormbrood card = new WhirlwingStormbrood();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).contains(card);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Dynamic Soar cannot target an opponent's creature")
    void omenRejectsOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhirlwingStormbrood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Whirlwing Stormbrood grants its controller flash for sorcery spells")
    void grantsFlashToSorcerySpells() {
        harness.addToBattlefield(player1, new WhirlwingStormbrood());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.ensurePriority(player1);

        harness.castSorcery(player1, 0, List.of());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void creatureFaceHasFlashWithoutAnotherStormbrood() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new WhirlwingStormbrood()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Whirlwing Stormbrood");
    }

    @Test
    void grantsFlashToDragonCreatureSpells() {
        harness.addToBattlefield(player1, new WhirlwingStormbrood());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new RunescaleStormbrood()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runescale Stormbrood");
    }

    @Test
    void doesNotGrantFlashToNonDragonCreatures() {
        harness.addToBattlefield(player1, new WhirlwingStormbrood());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantFlashToOpponentsSorceries() {
        harness.addToBattlefield(player2, new WhirlwingStormbrood());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sorceryOmenDoesNotInheritCreatureFacesFlash() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RunescaleStormbrood());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new WhirlwingStormbrood()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantsFlashToSorceryOmen() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WhirlwingStormbrood());
        WhirlwingStormbrood card = new WhirlwingStormbrood();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.ensurePriority(player1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
    }

    @Test
    void omenGoesToGraveyardWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WhirlwingStormbrood());
        WhirlwingStormbrood card = new WhirlwingStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castWithAlternateCost(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void omenRechecksThatControllerStillControlsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WhirlwingStormbrood());
        WhirlwingStormbrood card = new WhirlwingStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castWithAlternateCost(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void flashPermissionEndsWhenStormbroodLeavesBattlefield() {
        Permanent stormbrood = harness.addToBattlefieldAndReturn(player1, new WhirlwingStormbrood());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new RunescaleStormbrood()));
        harness.addMana(player1, ManaColor.RED, 4);
        gd.playerBattlefields.get(player1.getId()).remove(stormbrood);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void omenRejectsNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WhirlwingStormbrood()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
