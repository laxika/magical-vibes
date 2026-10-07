package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderWomanSecretAgent.class, GrizzlyBears.class, ControlMagic.class, Twiddle.class})
class SpiderWomanSecretAgentTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps an opponent's creature and prevents it from untapping")
    void entersTapsAndLocksOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSpiderWoman(bears.getId());

        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap lock ends when Spider-Woman leaves the battlefield")
    void untapLockEndsWhenSourceLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSpiderWoman(bears.getId());
        Permanent spiderWoman = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpiderWomanSecretAgent)
                .findFirst()
                .orElseThrow();

        gd.playerBattlefields.get(player1.getId()).remove(spiderWoman);
        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpiderWomanSecretAgent()));
        addManaForSpiderWoman();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBears.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSpiderWoman(UUID targetId) {
        harness.setHand(player1, List.of(new SpiderWomanSecretAgent()));
        addManaForSpiderWoman();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForSpiderWoman() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("The locked creature cannot be untapped by a spell")
    void lockPreventsUntapSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSpiderWoman(bears.getId());
        harness.setHand(player2, List.of(new Twiddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The lock ends when the ability's controller loses control of Spider-Woman")
    void lockEndsWhenSourceChangesController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSpiderWoman(bears.getId());
        UUID spiderWomanId = harness.getPermanentId(player1, "Spider-Woman, Secret Agent");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player2, 0, spiderWomanId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Spider-Woman, Secret Agent");
        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped creature is still locked")
    void alreadyTappedCreatureIsLocked() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();

        castSpiderWoman(bears.getId());
        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flash allows Spider-Woman to enter during an opponent's upkeep")
    void canEnterDuringOpponentsUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        castSpiderWoman(bears.getId());

        harness.assertOnBattlefield(player1, "Spider-Woman, Secret Agent");
        assertThat(bears.isTapped()).isTrue();
    }
}
