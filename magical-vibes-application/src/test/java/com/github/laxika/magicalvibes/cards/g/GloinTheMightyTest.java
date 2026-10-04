package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EasyPickings;
import com.github.laxika.magicalvibes.cards.w.WoodElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloinTheMighty.class, EasyPickings.class, GrizzlyBears.class, WoodElves.class})
class GloinTheMightyTest extends BaseCardTest {

    @Test
    void addsTwoRedManaAtBeginningOfControllerFirstMainPhase() {
        harness.addToBattlefield(player1, new GloinTheMighty());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void doesNotTriggerOnOpponentFirstMainPhase() {
        harness.addToBattlefield(player1, new GloinTheMighty());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void adventureDamagesOnlyOpponentsCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GloinTheMighty card = new GloinTheMighty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        GloinTheMighty card = new GloinTheMighty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glóin the Mighty");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void manaTriggerUsesTheStack() {
        harness.addToBattlefield(player1, new GloinTheMighty());

        advanceToPrecombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void doesNotAddManaInSecondMainPhase() {
        harness.addToBattlefield(player1, new GloinTheMighty());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void adventureKillsEachOpposingOneToughnessCreatureAndLeavesOwnCreatureAndPlayersUnharmed() {
        harness.addToBattlefield(player2, new WoodElves());
        harness.addToBattlefield(player2, new WoodElves());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WoodElves());
        GloinTheMighty card = new GloinTheMighty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wood Elves");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Wood Elves");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
