package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SelesnyaSagittars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Blockbuster.class, BorosRecruit.class, BorosSignet.class, SelesnyaSagittars.class})
class BlockbusterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Blockbuster deals 3 damage to each tapped creature and each player")
    void damagesTappedCreaturesAndPlayers() {
        Permanent blockbuster = harness.addToBattlefieldAndReturn(player1, new Blockbuster());
        Permanent tappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent untappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent tappedOpposingCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent untappedOpposingCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent tappedSurvivingCreature =
                harness.addToBattlefieldAndReturn(player1, new SelesnyaSagittars());
        Permanent tappedNoncreature = harness.addToBattlefieldAndReturn(player1, new BorosSignet());
        tappedOwnCreature.tap();
        tappedOpposingCreature.tap();
        tappedSurvivingCreature.tap();
        tappedNoncreature.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(untappedOwnCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untappedOpposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(tappedSurvivingCreature, tappedNoncreature);
        assertThat(tappedSurvivingCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(tappedNoncreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(blockbuster, tappedOwnCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tappedOpposingCreature);
        harness.assertInGraveyard(player1, "Blockbuster");
        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertInGraveyard(player2, "Boros Recruit");
    }

    @Test
    @DisplayName("Blockbuster is sacrificed before its ability resolves")
    void sacrificesAsActivationCost() {
        Permanent blockbuster = harness.addToBattlefieldAndReturn(player1, new Blockbuster());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        tappedCreature.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blockbuster);
        harness.assertInGraveyard(player1, "Blockbuster");
        harness.assertOnBattlefield(player1, "Boros Recruit");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Boros Recruit");
    }

    @Test
    @DisplayName("Blockbuster can be activated while tapped because tapping is not part of its cost")
    void canActivateWhileTapped() {
        Permanent blockbuster = harness.addToBattlefieldAndReturn(player1, new Blockbuster());
        blockbuster.tap();
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        tappedCreature.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Blockbuster");
        harness.assertNotOnBattlefield(player1, "Boros Recruit");
    }

    @Test
    @DisplayName("Blockbuster still damages each player when no creature is tapped")
    void damagesPlayersWithoutTappedCreatures() {
        harness.addToBattlefield(player1, new Blockbuster());
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(untappedCreature);
        assertThat(untappedCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Blockbuster");
    }
}
