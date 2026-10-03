package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwarvenMauler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BilbosDeadlySlice.class, Forest.class, DwarvenMauler.class})
class BilbosDeadlySliceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys the target creature")
    void resolvingDestroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwarvenMauler());
        harness.setHand(player1, List.of(new BilbosDeadlySlice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Dwarven Mauler");
        harness.assertInGraveyard(player2, "Dwarven Mauler");
        harness.assertInGraveyard(player1, "Bilbo's Deadly Slice");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new DwarvenMauler());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BilbosDeadlySlice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwarvenMauler());
        harness.setHand(player1, List.of(new BilbosDeadlySlice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Bilbo's Deadly Slice");
    }

    @Test
    void canDestroyYourOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DwarvenMauler());
        harness.setHand(player1, List.of(new BilbosDeadlySlice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Dwarven Mauler");
        harness.assertInGraveyard(player1, "Dwarven Mauler");
        harness.assertInGraveyard(player1, "Bilbo's Deadly Slice");
    }

    @Test
    void indestructibleCreatureSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwarvenMauler());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new BilbosDeadlySlice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Dwarven Mauler");
        harness.assertNotInGraveyard(player2, "Dwarven Mauler");
        harness.assertInGraveyard(player1, "Bilbo's Deadly Slice");
    }

    @Test
    void regenerationShieldPreventsDestruction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwarvenMauler());
        creature.setRegenerationShield(1);
        harness.setHand(player1, List.of(new BilbosDeadlySlice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Dwarven Mauler");
        harness.assertNotInGraveyard(player2, "Dwarven Mauler");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Bilbo's Deadly Slice");
    }

    @Test
    void targetGainingHexproofBeforeResolutionSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwarvenMauler());
        harness.setHand(player1, List.of(new BilbosDeadlySlice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        creature.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dwarven Mauler");
        harness.assertNotInGraveyard(player2, "Dwarven Mauler");
        harness.assertInGraveyard(player1, "Bilbo's Deadly Slice");
    }
}
