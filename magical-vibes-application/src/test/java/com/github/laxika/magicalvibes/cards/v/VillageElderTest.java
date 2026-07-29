package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VillageElderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest regenerates the targeted creature")
    void regeneratesTargetCreature() {
        Permanent elder = addElderReady(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(elder.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Can regenerate an opponent's creature")
    void canRegenerateOpponentCreature() {
        addElderReady(player1);
        harness.addToBattlefield(player1, new Forest());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a Forest to sacrifice")
    void cannotActivateWithoutForest() {
        addElderReady(player1);
        harness.addToBattlefield(player1, new Plains());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
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
        Permanent perm = new Permanent(new VillageElder());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
