package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.b.BreedingPool;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WreckingBall.class, AssaultZeppelid.class, BreedingPool.class, AzoriusSignet.class})
class WreckingBallTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature")
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());

        castWreckingBall(target);

        harness.assertNotOnBattlefield(player2, "Assault Zeppelid");
        harness.assertInGraveyard(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Destroys a creature you control")
    void destroysYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());

        castWreckingBall(target);

        harness.assertNotOnBattlefield(player1, "Assault Zeppelid");
        harness.assertInGraveyard(player1, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Destroys a target land")
    void destroysTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BreedingPool());

        castWreckingBall(target);

        harness.assertNotOnBattlefield(player2, "Breeding Pool");
        harness.assertInGraveyard(player2, "Breeding Pool");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonland permanent")
    void cannotTargetNoncreatureNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.setHand(player1, List.of(new WreckingBall()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or land");
    }

    private void castWreckingBall(Permanent target) {
        harness.setHand(player1, List.of(new WreckingBall()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
