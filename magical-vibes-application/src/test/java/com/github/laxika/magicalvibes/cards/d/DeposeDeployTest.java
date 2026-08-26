package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeposeDeployTest extends BaseCardTest {

    private static final int DEPOSE = 0;
    private static final int DEPLOY = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Depose taps a creature and draws a card")
    void deposeTapsAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeposeMana();

        harness.castModalInstant(player1, 0, DEPOSE, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Depose cannot target a player")
    void deposeCannotTargetPlayer() {
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeposeMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, DEPOSE, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deploy creates two flying Thopters and gains life for all creatures")
    void deployCreatesThoptersAndGainsLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeployMana();

        harness.castModalInstant(player1, 0, DEPLOY, List.of());
        harness.passBothPriorities();

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Fuse resolves Depose before Deploy")
    void fuseResolvesBothHalves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addFuseMana();

        harness.castModalInstant(player1, 0, FUSE, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Fuse requires the combined mana cost")
    void fuseRequiresBothHalvesCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeployMana();

        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addDeposeMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addDeployMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addFuseMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
