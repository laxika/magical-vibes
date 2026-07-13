package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValleymakerTest extends BaseCardTest {

    // ===== {T}, Sacrifice a Mountain: deal 3 damage to target creature =====

    @Test
    @DisplayName("Sacrificing a Mountain deals 3 damage to target creature")
    void mountainAbilityDealsThreeDamage() {
        addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new AirElemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Air Elemental");
        assertThat(target).isNotNull();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"));
    }

    @Test
    @DisplayName("Mountain ability cannot target a non-creature")
    void mountainAbilityRejectsNonCreatureTarget() {
        addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== {T}, Sacrifice a Forest: Choose a player. That player adds {G}{G}{G} =====

    @Test
    @DisplayName("Forest ability adds {G}{G}{G} to the chosen controller's mana pool")
    void forestAbilityAddsManaToController() {
        addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        // Mana ability: no stack, pauses only to choose the recipient player.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Forest ability can add {G}{G}{G} to a chosen opponent's mana pool")
    void forestAbilityAddsManaToOpponent() {
        addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }
}
