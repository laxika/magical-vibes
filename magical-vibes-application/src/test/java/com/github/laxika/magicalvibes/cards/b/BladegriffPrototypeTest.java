package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladegriffPrototype.class, GrizzlyBears.class, Mountain.class})
class BladegriffPrototypeTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged player chooses a nonland permanent controlled by an opponent")
    void damagedPlayerChoosesOpponentControlledNonlandPermanent() {
        Permanent bladegriff = addCreatureReady(player1, new BladegriffPrototype());
        bladegriff.setAttacking(true);
        Permanent ownPermanent = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(target.getId())
                .doesNotContain(ownPermanent.getId(), land.getId());
    }

    @Test
    @DisplayName("The chosen nonland permanent is destroyed")
    void destroysChosenPermanent() {
        Permanent bladegriff = addCreatureReady(player1, new BladegriffPrototype());
        bladegriff.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
