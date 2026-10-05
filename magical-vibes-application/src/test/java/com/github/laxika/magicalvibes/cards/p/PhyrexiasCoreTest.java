package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexiasCore.class, DarksteelRelic.class, PristineTalisman.class})
class PhyrexiasCoreTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new PhyrexiasCore());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Phyrexia's Core").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice ability gains 1 life on resolution")
    void sacrificeArtifactGainsLife() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertNotOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability without mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addToBattlefield(player1, new DarksteelRelic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With multiple artifacts, asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice and tap are paid before life gain resolves")
    void costsArePaidBeforeResolution() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new PhyrexiasCore());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(core.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertNotOnBattlefield(player1, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addToBattlefield(player2, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Darksteel Relic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped artifact can be sacrificed")
    void canSacrificeTappedArtifact() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        artifact.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pristine Talisman");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("A tapped Core cannot activate either tap ability")
    void tappedCoreCannotActivate() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new PhyrexiasCore());
        core.setTapped(true);
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact sacrifices only that artifact and completes the ability")
    void chosenArtifactIsSacrificed() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addToBattlefield(player1, new DarksteelRelic());
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.addToBattlefield(player2, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, talisman.getId());

        harness.assertInGraveyard(player1, "Pristine Talisman");
        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player2, "Darksteel Relic");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.stack).isEmpty();
    }

}
