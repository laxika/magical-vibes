package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeposeDeploy.class, SauroformHybrid.class})
class DeposeDeployTest extends BaseCardTest {

    private static final int DEPOSE = 0;
    private static final int DEPLOY = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Depose taps a creature and draws a card")
    void deposeTapsAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.setLibrary(player1, List.of(new SauroformHybrid()));

        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeposeMana();

        harness.castModalInstant(player1, 0, DEPOSE, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Sauroform Hybrid");
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
        harness.addToBattlefield(player1, new SauroformHybrid());
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeployMana();

        harness.castModalInstant(player1, 0, DEPLOY, List.of());
        harness.passBothPriorities();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
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
    @DisplayName("Both halves cannot be cast together even with sufficient mana")
    void cannotFuseBothHalves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addFuseMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deploy cannot be cast with only enough mana for Depose")
    void deployRequiresItsOwnCost() {
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeposeMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, DEPLOY, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Depose accepts blue mana and draws even when its own target is already tapped")
    void deposeWithBlueManaCanTargetOwnTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        target.tap();
        harness.setLibrary(player1, List.of(new SauroformHybrid()));
        harness.setHand(player1, List.of(new DeposeDeploy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, DEPOSE, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Sauroform Hybrid");
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Depose does not draw when its only target leaves the battlefield")
    void deposeDoesNotDrawWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.setLibrary(player1, List.of(new SauroformHybrid()));
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeposeMana();

        harness.castModalInstant(player1, 0, DEPOSE, List.of(target.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Sauroform Hybrid");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Depose // Deploy");
    }

    @Test
    @DisplayName("Depose requires a creature target")
    void deposeCannotBeCastWithoutTarget() {
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeposeMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, DEPOSE, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deploy gains two life on an empty battlefield and creates colorless 1/1 Thopters")
    void deployOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeployMana();

        harness.castModalInstant(player1, 0, DEPLOY, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allSatisfy(thopter -> {
            assertThat(thopter.getCard().isToken()).isTrue();
            assertThat(thopter.getCard().getPower()).isEqualTo(1);
            assertThat(thopter.getCard().getToughness()).isEqualTo(1);
            assertThat(thopter.getCard().getColors()).isEmpty();
            assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
            assertThat(thopter.isTapped()).isFalse();
        });
        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Deploy counts creatures at resolution and excludes opposing creatures")
    void deployCountsOnlyCurrentControlledCreatures() {
        harness.addToBattlefield(player2, new SauroformHybrid());
        harness.setHand(player1, List.of(new DeposeDeploy()));
        addDeployMana();

        harness.castModalInstant(player1, 0, DEPLOY, List.of());
        harness.addToBattlefield(player1, new SauroformHybrid());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
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
