package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MishrasHelix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scald.class, Forest.class, Island.class, MishrasHelix.class})
class ScaldTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping your Island for mana deals 1 damage to you")
    void controllerTapsIsland() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player1, new Island());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping an opponent's Island for mana deals 1 damage to that opponent")
    void opponentTapsIsland() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player2, new Island());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping a non-Island land for mana does not trigger Scald")
    void nonIslandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Island tap triggers Scald separately")
    void multipleIslandTapsTriggerSeparately() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        harness.tapPermanent(player1, 2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Tapping an Island without using its mana ability does not trigger Scald")
    void nonManaTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player1, new MishrasHelix());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbilityWithMultiTargets(player1, 1, 0, 1, List.of(island.getId()));
        resolveAllTriggers();

        assertThat(island.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
