package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProteanRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Raid lets Protean Raider enter as a copy of a creature")
    void copiesCreatureWithRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfMercy());
        castProteanRaider();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(findRaider()).isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Protean Raider does not offer the copy when its controller did not attack")
    void doesNotCopyWithoutRaid() {
        harness.addToBattlefield(player2, new AngelOfMercy());
        castProteanRaider();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findRaider()).isNotNull();
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy Protean Raider's raid")
    void opponentAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.addToBattlefield(player2, new AngelOfMercy());
        castProteanRaider();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findRaider()).isNotNull();
    }

    private void castProteanRaider() {
        harness.setHand(player1, List.of(new ProteanRaider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
    }

    private Permanent findRaider() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof ProteanRaider)
                .findFirst()
                .orElse(null);
    }
}
