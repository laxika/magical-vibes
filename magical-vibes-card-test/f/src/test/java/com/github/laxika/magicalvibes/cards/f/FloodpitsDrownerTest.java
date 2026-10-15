package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PatchworkBeastie;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloodpitsDrowner.class, PatchworkBeastie.class, CosisTrickster.class})
class FloodpitsDrownerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps an opponent's creature and puts a stun counter on it")
    void etbTapsAndStunsOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());
        harness.setHand(player1, List.of(new FloodpitsDrowner()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability shuffles the source and a creature with a stun counter into their owners' libraries")
    void abilityShufflesSourceAndStunnedCreature() {
        addReadyDrowner(player1);
        Permanent target = addCreatureReady(player2, new PatchworkBeastie());
        target.setCounterCount(CounterType.STUN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Floodpits Drowner");
        harness.assertNotOnBattlefield(player2, "Patchwork Beastie");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .contains("Floodpits Drowner");
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(Card::getName))
                .contains("Patchwork Beastie");
    }

    @Test
    @DisplayName("Ability can target Floodpits Drowner itself when it has a stun counter")
    void abilityCanTargetItself() {
        Permanent source = addReadyDrowner(player1);
        source.setCounterCount(CounterType.STUN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Floodpits Drowner");
        assertThat(gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> card instanceof FloodpitsDrowner))
                .hasSize(1);
    }

    @Test
    @DisplayName("Ability cannot target a creature without a stun counter")
    void abilityCannotTargetCreatureWithoutStunCounter() {
        addReadyDrowner(player1);
        Permanent target = addCreatureReady(player2, new PatchworkBeastie());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingLastStunCounterMakesAbilityDoNothing() {
        Permanent source = addReadyDrowner(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());
        target.setCounterCount(CounterType.STUN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.STUN, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Floodpits Drowner");
        harness.assertOnBattlefield(player2, "Patchwork Beastie");
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void shufflingTwoCreaturesWithSameOwnerShufflesLibraryOnlyOnce() {
        addReadyDrowner(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PatchworkBeastie());
        harness.addToBattlefield(player2, new CosisTrickster());
        target.setCounterCount(CounterType.STUN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
    }

    @Test
    void sourceOwnerLibraryStillShufflesWhenSourceHasLeftBattlefield() {
        Permanent source = addReadyDrowner(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());
        harness.addToBattlefield(player2, new CosisTrickster());
        target.setCounterCount(CounterType.STUN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Patchwork Beastie");
        harness.assertInGraveyard(player1, "Floodpits Drowner");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
    }
    private Permanent addReadyDrowner(Player player) {
        return addCreatureReady(player, new FloodpitsDrowner());
    }
}
