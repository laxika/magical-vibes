package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScarbladesMaliceTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Grants a creature you control deathtouch and lifelink until end of turn")
    void grantsDeathtouchAndLifelink() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creates a 2/2 black and green Elf when the targeted creature dies this turn")
    void createsElfWhenTargetedCreatureDies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, targetId);
        resolveStack();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Elf");
    }

    @Test
    @DisplayName("Does not create an Elf if the targeted creature survives")
    void doesNotCreateElfIfTargetSurvives() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Elf");
    }

    @Test
    @DisplayName("Keyword grants wear off at end of turn")
    void keywordGrantsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
