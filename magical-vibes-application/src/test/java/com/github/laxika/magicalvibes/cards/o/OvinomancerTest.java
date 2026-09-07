package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ovinomancer.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class,
        TerramorphicExpanse.class})
class OvinomancerTest extends BaseCardTest {

    private long basicLandsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC))
                .count();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }

    private void castOvinomancer() {
        harness.setHand(player1, List.of(new Ovinomancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has fewer than three basic lands")
    void autoSacrificesWithoutThreeBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        castOvinomancer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        harness.assertInGraveyard(player1, "Ovinomancer");
        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count nonbasic lands toward the three-land requirement")
    void doesNotCountNonbasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        castOvinomancer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        harness.assertInGraveyard(player1, "Ovinomancer");
        harness.assertOnBattlefield(player1, "Terramorphic Expanse");
        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Prompts a may ability when controller has three or more basic lands")
    void promptsMayAbilityWithThreeBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        castOvinomancer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly three basic lands returns them and keeps Ovinomancer")
    void acceptWithExactlyThreeBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        castOvinomancer();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND)).count()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Ovinomancer");
    }

    @Test
    @DisplayName("Accepting with four basic lands lets controller choose which three to return")
    void acceptWithFourBasicLandsChoosesThree() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        castOvinomancer();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> landIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .map(Permanent::getId)
                .limit(3)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, landIds);

        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ovinomancer");
    }

    @Test
    @DisplayName("Declining sacrifices Ovinomancer and keeps the lands")
    void declineSacrificesOvinomancer() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        castOvinomancer();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        harness.assertInGraveyard(player1, "Ovinomancer");
        assertThat(basicLandsControlledBy(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Activated ability destroys target and gives its controller a Sheep token")
    void activatedAbilityDestroysAndCreatesSheep() {
        addCreatureReady(player1, new Ovinomancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, "Ovinomancer"), null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Ovinomancer"))).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        Permanent sheep = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Sheep"))
                .findFirst().orElseThrow();
        assertThat(sheep.getCard().getPower()).isEqualTo(0);
        assertThat(sheep.getCard().getToughness()).isEqualTo(1);
        assertThat(sheep.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(sheep.getCard().getSubtypes()).contains(CardSubtype.SHEEP);
    }

    @Test
    @DisplayName("Target creature can't be regenerated by the activated ability")
    void activatedAbilityIgnoresRegeneration() {
        addCreatureReady(player1, new Ovinomancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        UUID targetId = bears.getId();
        bears.setRegenerationShield(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, "Ovinomancer"), null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Self-targeting fails after returning Ovinomancer as the activation cost")
    void selfTargetBecomesIllegalAfterReturningOvinomancer() {
        addCreatureReady(player1, new Ovinomancer());
        UUID targetId = harness.getPermanentId(player1, "Ovinomancer");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, "Ovinomancer"), null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ovinomancer");
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Ovinomancer"))).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Sheep"))).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Sheep"))).isTrue();
    }
}
