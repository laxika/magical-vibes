package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.PhyrexianLens;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerduranEmissary.class, PhyrexianLens.class, YavimayaBarbarian.class})
class VerduranEmissaryTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotDestroy() {
        harness.addToBattlefield(player2, new PhyrexianLens());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Verduran Emissary");
        harness.assertOnBattlefield(player2, "Phyrexian Lens");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedDestroysTargetArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianLens());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Lens");
        harness.assertInGraveyard(player2, "Phyrexian Lens");
    }

    @Test
    void kickedDestroyCannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianLens());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        target.setRegenerationShield(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Lens");
        harness.assertInGraveyard(player2, "Phyrexian Lens");
    }

    @Test
    void cannotKickTargetNonArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void kickedChoosesOnlyArtifactTargetsAtEtbTime() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PhyrexianLens());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds())
                .containsExactly(artifact.getId())
                .doesNotContain(creature.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phyrexian Lens");
        harness.assertOnBattlefield(player2, "Yavimaya Barbarian");
    }

    @Test
    void kickedWithoutArtifactDoesNotCreateTrigger() {
        harness.addToBattlefield(player2, new YavimayaBarbarian());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Verduran Emissary");
        harness.assertOnBattlefield(player2, "Yavimaya Barbarian");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
