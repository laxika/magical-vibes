package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.g.GlacialChasm;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TownRazerTyrant.class, HostileDesert.class, Island.class, GlacialChasm.class})
class TownRazerTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Removes non-mana abilities from an opposing nonbasic land but preserves mana abilities")
    void removesNonManaAbilitiesFromTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        castTyrant(target);

        assertThat(gqs.computeStaticBonus(gd, target).losesAllNonManaAbilities()).isTrue();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setGraveyard(player2, List.of(new Island()));
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("At the land controller's upkeep, declining the sacrifice deals 2 damage")
    void dealsDamageWhenSacrificeIsDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        castTyrant(target);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertOnBattlefield(player2, "Hostile Desert");
    }

    @Test
    @DisplayName("At the land controller's upkeep, accepting the sacrifice sacrifices the land")
    void sacrificesTheLandWhenAccepted() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        castTyrant(target);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Hostile Desert");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only an opposing nonbasic land can be targeted")
    void rejectsBasicAndOwnLands() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new HostileDesert());
        harness.setHand(player1, List.of(new TownRazerTyrant()));
        addTyrantMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent opposingBasic = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new TownRazerTyrant()));
        addTyrantMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opposingBasic.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removed printed upkeep abilities do not trigger")
    void removesPrintedUpkeepAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlacialChasm());
        castTyrant(target);

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Glacial Chasm");
    }

    @Test
    @DisplayName("The granted ability does not trigger during the Tyrant controller's upkeep")
    void doesNotTriggerOnOtherPlayersUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        castTyrant(target);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Applying the effect twice replaces the old non-mana trigger")
    void secondTyrantLeavesOnlyOneUpkeepTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        castTyrant(target);
        castTyrant(target);

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The land remains affected after the Tyrant leaves the battlefield")
    void effectPersistsAfterTyrantLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        castTyrant(target);
        gd.playerBattlefields.get(player1.getId()).clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 18);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setGraveyard(player2, List.of(new Island()));
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTyrant(Permanent target) {
        harness.setHand(player1, List.of(new TownRazerTyrant()));
        addTyrantMana();
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void addTyrantMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
