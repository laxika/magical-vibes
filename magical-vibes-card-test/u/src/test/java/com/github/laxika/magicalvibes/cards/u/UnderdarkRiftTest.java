package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderdarkRift.class, GrizzlyBears.class, MindStone.class})
class UnderdarkRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Rolls a d10 and puts the target just beneath that many cards")
    void rollsAndPutsTargetIntoLibrary() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var library = new ArrayList<GrizzlyBears>();
        for (int i = 0; i < 11; i++) {
            library.add(new GrizzlyBears());
        }
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rift.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerDecks.get(player2.getId()).indexOf(target.getCard())).isBetween(1, 10);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rift.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rift);
        assertThat(rift.isTapped()).isFalse();
    }

    @Test
    void tapsForColorlessManaWithoutUsingTheStack() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());

        harness.tapPermanent(player1, 0);

        assertThat(rift.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rift);
    }

    @Test
    void putsArtifactIntoEmptyLibraryAndPaysCostsBeforeResolution() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(rift.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rift);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rift.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    void putsStolenArtifactIntoOwnersLibrary() {
        harness.addToBattlefield(player1, new UnderdarkRift());
        MindStone stone = new MindStone();
        stone.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, stone);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(stone);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rift);
        assertThat(rift.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithInsufficientMana() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rift);
        assertThat(rift.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rift);
        assertThat(rift.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnTheStack() {
        harness.addToBattlefield(player1, new UnderdarkRift());
        Permanent secondRift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondRift);
        assertThat(secondRift.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    void cannotPayTapCostAfterTappingForMana() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rift);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(rift.getCard());
        assertThat(gd.stack).isEmpty();
    }
}
