package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NosyGoblin.class, AphettoAlchemist.class})
class NosyGoblinTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndDestroysFaceDownCreature() {
        addCreatureReady(player1, new NosyGoblin());
        Permanent target = addCreatureReady(player2, new AphettoAlchemist());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nosy Goblin");
        harness.assertNotOnBattlefield(player1, "Nosy Goblin");
        harness.assertInGraveyard(player2, "Aphetto Alchemist");
        harness.assertNotOnBattlefield(player2, "Aphetto Alchemist");
    }

    @Test
    void cannotTargetFaceUpCreature() {
        addCreatureReady(player1, new NosyGoblin());
        Permanent target = addCreatureReady(player2, new AphettoAlchemist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nosy Goblin");
        harness.assertOnBattlefield(player2, "Aphetto Alchemist");
    }

    @Test
    void cannotTargetFaceDownNoncreature() {
        addCreatureReady(player1, new NosyGoblin());
        Permanent target = addCreatureReady(player2, new AphettoAlchemist());
        target.setFaceDown(2, 2, Set.of(CardType.LAND));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nosy Goblin");
        harness.assertOnBattlefield(player2, "Aphetto Alchemist");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new NosyGoblin());
        Permanent target = addCreatureReady(player2, new AphettoAlchemist());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        source.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nosy Goblin");
        harness.assertOnBattlefield(player2, "Aphetto Alchemist");
    }

    @Test
    void doesNotDestroyTargetThatTurnsFaceUpBeforeResolution() {
        addCreatureReady(player1, new NosyGoblin());
        Permanent target = addCreatureReady(player2, new AphettoAlchemist());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        target.turnFaceUp();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nosy Goblin");
        harness.assertOnBattlefield(player2, "Aphetto Alchemist");
        harness.assertNotInGraveyard(player2, "Aphetto Alchemist");
    }
}
