package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillageElder.class, Forest.class, GiantMantis.class, Plains.class})
class VillageElderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest regenerates the targeted creature")
    void regeneratesTargetCreature() {
        Permanent elder = addElderReady(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent mantis = addCreatureReady(player1, new GiantMantis());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, mantis.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(mantis.getRegenerationShield()).isEqualTo(1);
        assertThat(elder.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Can regenerate an opponent's creature")
    void canRegenerateOpponentCreature() {
        addElderReady(player1);
        harness.addToBattlefield(player1, new Forest());
        Permanent opponentMantis = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentMantis.getId());
        harness.passBothPriorities();

        assertThat(opponentMantis.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhenTapped() {
        Permanent elder = addElderReady(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent mantis = addCreatureReady(player1, new GiantMantis());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, mantis.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(elder.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mantis.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a Forest to sacrifice")
    void cannotActivateWithoutForest() {
        addElderReady(player1);
        harness.addToBattlefield(player1, new Plains());
        Permanent mantis = addCreatureReady(player1, new GiantMantis());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mantis.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addElderReady(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addElderReady(Player player) {
        return addCreatureReady(player, new VillageElder());
    }
}
