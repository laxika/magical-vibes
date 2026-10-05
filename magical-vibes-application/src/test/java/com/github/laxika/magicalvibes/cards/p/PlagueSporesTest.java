package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.cards.t.TidalVisionary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlagueSpores.class, Boomerang.class, GrizzlyBears.class, HillGiant.class, MassOfGhouls.class,
        Mountain.class, TreetopVillage.class, TidalVisionary.class})
class PlagueSporesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the nonblack creature and land without allowing regeneration")
    void destroysNonblackCreatureAndLandWithoutRegeneration() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        creature.setRegenerationShield(1);
        land.setRegenerationShield(1);
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId(), land.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("First target must be a nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a land as the first target")
    void cannotTargetLandAsFirstTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("First target must be a nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent as the second target")
    void cannotTargetNonlandAsSecondTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), otherCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Can choose the same nonblack creature land for both targets")
    void canChooseSameNonblackCreatureLandForBothTargets() {
        Permanent creatureLand = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.of(creatureLand.getId(), creatureLand.getId()));

        harness.assertInGraveyard(player1, "Treetop Village");
    }

    @Test
    @DisplayName("Still destroys the land when the creature target leaves the battlefield")
    void destroysLandWhenCreatureTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        land.setRegenerationShield(1);
        prepareCast();
        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Plague Spores");
    }

    @Test
    @DisplayName("Still destroys the creature when the land target leaves the battlefield")
    void destroysCreatureWhenLandTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        creature.setRegenerationShield(1);
        prepareCast();
        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Plague Spores");
    }

    @Test
    @DisplayName("Does not resolve when the shared creature land target leaves the battlefield")
    void doesNotResolveWhenSharedTargetLeaves() {
        Permanent creatureLand = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        prepareCast();
        harness.castSorcery(player1, 0, List.of(creatureLand.getId(), creatureLand.getId()));

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, creatureLand.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Treetop Village");
        harness.assertNotInGraveyard(player1, "Treetop Village");
        harness.assertInGraveyard(player1, "Plague Spores");
    }

    @Test
    @DisplayName("A creature that becomes black is spared while the land is destroyed")
    void sparesCreatureThatBecomesBlack() {
        addCreatureReady(player2, new TidalVisionary());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        prepareCast();
        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLACK");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("A shared creature land that becomes black remains a legal land target")
    void destroysSharedCreatureLandThatBecomesBlack() {
        addCreatureReady(player2, new TidalVisionary());
        Permanent creatureLand = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        creatureLand.setRegenerationShield(1);
        prepareCast();
        harness.castSorcery(player1, 0, List.of(creatureLand.getId(), creatureLand.getId()));

        harness.activateAbility(player2, 0, null, creatureLand.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLACK");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Treetop Village");
        harness.assertNotOnBattlefield(player1, "Treetop Village");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new PlagueSpores()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
