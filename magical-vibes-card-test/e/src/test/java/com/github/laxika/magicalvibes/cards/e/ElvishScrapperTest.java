package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishScrapper.class, GrizzlyBears.class, Island.class, RodOfRuin.class, PhyrexianHulk.class})
class ElvishScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Elvish Scrapper and destroys target artifact")
    void destroysTargetArtifact() {
        addCreatureReady(player1, new ElvishScrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elvish Scrapper");
        harness.assertInGraveyard(player1, "Elvish Scrapper");
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Can target own artifact")
    void canTargetOwnArtifact() {
        addCreatureReady(player1, new ElvishScrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rod of Ruin");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new ElvishScrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness (tap cost)")
    void cannotActivateWithSummoningSickness() {
        ElvishScrapper card = new ElvishScrapper();
        harness.addToBattlefield(player1, card);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileAlreadyTapped() {
        Permanent scrapper = addCreatureReady(player1, new ElvishScrapper());
        scrapper.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new ElvishScrapper());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an artifact creature")
    void canTargetArtifactCreature() {
        addCreatureReady(player1, new ElvishScrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Hulk");
        harness.assertInGraveyard(player2, "Phyrexian Hulk");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new ElvishScrapper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target artifact leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new ElvishScrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Pays the sacrifice cost when the ability is activated")
    void paysSacrificeCostOnActivation() {
        addCreatureReady(player1, new ElvishScrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Elvish Scrapper");
        harness.assertOnBattlefield(player2, "Rod of Ruin");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot pay the green activation cost with blue mana")
    void cannotActivateWithWrongManaColor() {
        Permanent scrapper = addCreatureReady(player1, new ElvishScrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scrapper.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Elvish Scrapper");
        harness.assertNotInGraveyard(player1, "Elvish Scrapper");
        harness.assertOnBattlefield(player2, "Rod of Ruin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a target and does not sacrifice the source")
    void cannotActivateWithoutTarget() {
        Permanent scrapper = addCreatureReady(player1, new ElvishScrapper());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scrapper.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Elvish Scrapper");
        harness.assertNotInGraveyard(player1, "Elvish Scrapper");
        assertThat(gd.stack).isEmpty();
    }
}
