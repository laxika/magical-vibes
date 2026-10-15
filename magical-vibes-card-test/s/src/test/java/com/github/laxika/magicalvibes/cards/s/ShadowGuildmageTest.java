package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowGuildmage.class, BayFalcon.class, Island.class})
class ShadowGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{U}, {T}: puts target creature you control on top of its owner's library")
    void tucksControlledCreature() {
        Permanent guildmage = addCreatureReady(player1, new ShadowGuildmage());
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        assertThat(gd.playerDecks.get(player1.getId()).get(0))
                .isSameAs(falcon.getCard());
    }

    @Test
    @DisplayName("The tuck ability cannot target a creature you don't control")
    void tuckRejectsOpponentCreature() {
        addCreatureReady(player1, new ShadowGuildmage());
        Permanent falcon = addCreatureReady(player2, new BayFalcon());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, falcon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The tuck ability cannot target a noncreature permanent")
    void tuckRejectsNonCreaturePermanent() {
        addCreatureReady(player1, new ShadowGuildmage());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The tuck ability can target Shadow Guildmage itself")
    void tucksItself() {
        Permanent guildmage = addCreatureReady(player1, new ShadowGuildmage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, guildmage.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shadow Guildmage");
        assertThat(gd.playerDecks.get(player1.getId()).get(0))
                .isSameAs(guildmage.getCard());
    }

    @Test
    @DisplayName("{R}, {T}: deals 1 damage to a player and 1 damage to you")
    void burnsPlayerAndController() {
        Permanent guildmage = addCreatureReady(player1, new ShadowGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife - 1);
    }

    @Test
    @DisplayName("{R}, {T}: deals 1 damage to a creature and 1 damage to you")
    void burnsCreatureAndController() {
        Permanent guildmage = addCreatureReady(player1, new ShadowGuildmage());
        Permanent falcon = addCreatureReady(player2, new BayFalcon());
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertInGraveyard(player2, "Bay Falcon");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife - 1);
    }

    @Test
    @DisplayName("The damage ability cannot target a noncreature permanent")
    void burnRejectsNonCreaturePermanent() {
        addCreatureReady(player1, new ShadowGuildmage());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Targeting yourself with the damage ability deals a total of 2 damage")
    void burnsControllerTwiceWhenTargetingSelf() {
        addCreatureReady(player1, new ShadowGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damaging Shadow Guildmage itself still deals the damage to its controller")
    void burnsItselfAndController() {
        Permanent guildmage = addCreatureReady(player1, new ShadowGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, guildmage.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shadow Guildmage");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An illegal damage target prevents the entire ability from resolving")
    void doesNotDamageControllerWhenTargetLeaves() {
        Permanent guildmage = addCreatureReady(player1, new ShadowGuildmage());
        addCreatureReady(player1, new ShadowGuildmage());
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, falcon.getId());
        harness.activateAbility(player1, 1, 0, null, falcon.getId());
        resolveAllTriggers();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(falcon.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The damage ability resolves even after Shadow Guildmage leaves the battlefield")
    void damageResolvesAfterSourceLeaves() {
        Permanent guildmage = addCreatureReady(player1, new ShadowGuildmage());
        addCreatureReady(player1, new ShadowGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.activateAbility(player1, 1, 0, null, guildmage.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(guildmage.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A controlled creature owned by the opponent goes to its owner's library")
    void tucksCreatureIntoOwnersLibrary() {
        addCreatureReady(player1, new ShadowGuildmage());
        BayFalcon card = new BayFalcon();
        card.setOwnerId(player2.getId());
        Permanent falcon = addCreatureReady(player1, card);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, falcon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
    }
}
