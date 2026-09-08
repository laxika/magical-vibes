package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlickImitatorTest extends BaseCardTest {

    @Test
    void atMaxSpeedSacrificesAndCopiesOwnSpell() {
        Permanent imitator = addCreatureReady(player1, new SlickImitator());
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.castInstant(player1, 0);
        harness.activateAbility(player1, 0, null, mercy.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(imitator);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> assertThat(copy.getDescription()).isEqualTo("Copy of Angel's Mercy"));
    }

    @Test
    void cannotActivateBelowMaxSpeed() {
        addCreatureReady(player1, new SlickImitator());
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerSpeeds.put(player1.getId(), 3);

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mercy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentSpell() {
        addCreatureReady(player1, new SlickImitator());
        AngelsMercy mercy = new AngelsMercy();
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(mercy));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);

        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mercy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
