package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoastalBulwark.class, Island.class, GrizzlyBears.class})
class CoastalBulwarkTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 while its controller controls an Island")
    void getsBoostWithIsland() {
        harness.addToBattlefield(player1, new CoastalBulwark());
        harness.addToBattlefield(player1, new Island());

        Permanent bulwark = findPermanent(player1, "Coastal Bulwark");
        assertThat(gqs.getEffectivePower(gd, bulwark)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bulwark)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get the boost without an Island")
    void noBoostWithoutIsland() {
        harness.addToBattlefield(player1, new CoastalBulwark());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bulwark = findPermanent(player1, "Coastal Bulwark");
        assertThat(gqs.getEffectivePower(gd, bulwark)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bulwark)).isEqualTo(3);
    }

    @Test
    @DisplayName("Surveil 1 puts the top card into the graveyard when accepted")
    void surveilAccepted() {
        Permanent bulwark = addCreatureReady(player1, new CoastalBulwark());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(bulwark.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining surveil 1 leaves the top card on the library")
    void surveilDeclined() {
        Permanent bulwark = addCreatureReady(player1, new CoastalBulwark());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(bulwark.isTapped()).isTrue();
    }

    @Test
    void islandBoostDoesNotAllowDefenderToAttack() {
        Permanent bulwark = addCreatureReady(player1, new CoastalBulwark());
        harness.addToBattlefield(player1, new Island());

        assertThat(als.canAttack(gd, bulwark, player1.getId())).isFalse();
    }
    @Test
    void opposingIslandDoesNotGrantBoost() {
        Permanent bulwark = harness.addToBattlefieldAndReturn(player1, new CoastalBulwark());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, bulwark)).isEqualTo(1);
    }

    @Test
    void multipleIslandsGrantOnlyOneBoostAndBoostEndsWhenLastLeaves() {
        Permanent bulwark = harness.addToBattlefieldAndReturn(player1, new CoastalBulwark());
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, bulwark)).isEqualTo(3);
        gd.playerBattlefields.get(player1.getId()).remove(firstIsland);
        assertThat(gqs.getEffectivePower(gd, bulwark)).isEqualTo(3);
        gd.playerBattlefields.get(player1.getId()).remove(secondIsland);
        assertThat(gqs.getEffectivePower(gd, bulwark)).isEqualTo(1);
    }

    @Test
    void surveilWithEmptyLibraryResolvesWithoutChoice() {
        Permanent bulwark = addCreatureReady(player1, new CoastalBulwark());
        harness.setLibrary(player1, List.of());
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(bulwark.isTapped()).isTrue();
    }

    @Test
    void surveilStillResolvesAfterSourceLeavesBattlefield() {
        Permanent bulwark = addCreatureReady(player1, new CoastalBulwark());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(bulwark);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent bulwark = harness.addToBattlefieldAndReturn(player1, new CoastalBulwark());
        bulwark.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(bulwark.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        Permanent bulwark = addCreatureReady(player1, new CoastalBulwark());
        bulwark.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent bulwark = addCreatureReady(player1, new CoastalBulwark());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(bulwark.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
