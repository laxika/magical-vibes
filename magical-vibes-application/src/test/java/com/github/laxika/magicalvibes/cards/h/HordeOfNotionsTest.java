package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Smokebraider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HordeOfNotionsTest extends BaseCardTest {

    private Permanent addReadyHorde() {
        Permanent horde = harness.addToBattlefieldAndReturn(player1, new HordeOfNotions());
        horde.setSummoningSick(false);
        return horde;
    }

    private void addWubrg(int copies) {
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, copies);
        }
    }

    @Test
    @DisplayName("Plays a targeted Elemental card from own graveyard without paying its mana cost")
    void playsElementalFromGraveyard() {
        addReadyHorde();
        Smokebraider elemental = new Smokebraider();
        harness.setGraveyard(player1, new ArrayList<>(List.of(elemental)));
        addWubrg(1);

        harness.activateAbility(player1, 0, 0, null, elemental.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities(); // resolve ability → queues may-play

        harness.handleMayAbilityChosen(player1, true);

        // Cast without paying → creature spell on the stack, no longer in graveyard.
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.CREATURE_SPELL
                && e.getCard() == elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities(); // resolve the creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == elemental);
    }

    @Test
    @DisplayName("Declining the may-play leaves the Elemental in the graveyard")
    void decliningLeavesCardInGraveyard() {
        addReadyHorde();
        Smokebraider elemental = new Smokebraider();
        harness.setGraveyard(player1, new ArrayList<>(List.of(elemental)));
        addWubrg(1);

        harness.activateAbility(player1, 0, 0, null, elemental.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(elemental);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() == elemental);
    }

    @Test
    @DisplayName("Cannot target a non-Elemental card in the graveyard")
    void cannotTargetNonElemental() {
        addReadyHorde();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        addWubrg(1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an Elemental card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addReadyHorde();
        Smokebraider elemental = new Smokebraider();
        harness.setGraveyard(player2, new ArrayList<>(List.of(elemental)));
        addWubrg(1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, elemental.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
