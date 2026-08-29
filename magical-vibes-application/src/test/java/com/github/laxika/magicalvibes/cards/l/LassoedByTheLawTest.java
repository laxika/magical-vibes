package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LassoedByTheLaw.class, Disperse.class, GrizzlyBears.class})
class LassoedByTheLawTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opposing nonland permanent and creates a Mercenary")
    void exilesPermanentAndCreatesMercenary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mercenary");
    }

    @Test
    @DisplayName("Mercenary boosts a creature you control at sorcery speed")
    void mercenaryBoostsCreatureYouControl() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAndResolve(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiled permanent returns when Lassoed by the Law leaves")
    void exiledPermanentReturnsWhenSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(target);
        Permanent source = findPermanent(player1, "Lassoed by the Law");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mercenary cannot target an opposing creature")
    void mercenaryCannotTargetOpposingCreature() {
        Permanent exiledCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(exiledCreature);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private void castAndResolve(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LassoedByTheLaw()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
