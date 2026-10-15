package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoggartArsonists.class, BlazethornScarecrow.class, Plains.class, Island.class})
class BoggartArsonistsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing to destroy a target Scarecrow removes both permanents")
    void destroysTargetScarecrow() {
        Permanent arsonists = addReadyArsonists(player1);
        Permanent scarecrow = addCreatureReady(player2, new BlazethornScarecrow());
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, scarecrow.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arsonists);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scarecrow);
        harness.assertInGraveyard(player1, "Boggart Arsonists");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Blazethorn Scarecrow");
        harness.assertInGraveyard(player2, "Blazethorn Scarecrow");
    }

    @Test
    @DisplayName("Can destroy a target Plains")
    void destroysTargetPlains() {
        addReadyArsonists(player1);
        Permanent plains = addCreatureReady(player2, new Plains());
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(plains);
        harness.assertInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("Cannot target a non-Scarecrow creature")
    void cannotTargetNonScarecrowCreature() {
        addReadyArsonists(player1);
        Permanent arsonists = addReadyArsonists(player2);
        addManaForAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, arsonists.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Plains land")
    void cannotTargetNonPlainsLand() {
        addReadyArsonists(player1);
        Permanent island = addCreatureReady(player2, new Island());
        addManaForAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyArsonists(player1);
        Permanent plains = addCreatureReady(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Plainswalk prevents blocking when the defender controls a Plains")
    void cannotBeBlockedWithDefendingPlains() {
        addReadyArsonists(player1);
        addCreatureReady(player2, new BlazethornScarecrow());
        harness.addToBattlefield(player2, new Plains());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attacker's Plains does not prevent the defender from blocking")
    void canBeBlockedWithoutDefendingPlains() {
        Permanent arsonists = addReadyArsonists(player1);
        harness.addToBattlefield(player1, new Plains());
        addCreatureReady(player2, new BlazethornScarecrow());
        harness.addToBattlefield(player2, new Island());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arsonists);
        harness.assertInGraveyard(player1, "Boggart Arsonists");
        harness.assertOnBattlefield(player2, "Blazethorn Scarecrow");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Arsonists can sacrifice itself to destroy its controller's Plains")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent arsonists = harness.addToBattlefieldAndReturn(player1, new BoggartArsonists());
        arsonists.setSummoningSick(true);
        arsonists.tap();
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boggart Arsonists");
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arsonists, plains);
    }

    private void addManaForAbility(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyArsonists(Player player) {
        return addCreatureReady(player, new BoggartArsonists());
    }
}
