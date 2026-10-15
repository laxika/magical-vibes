package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.r.Recommission;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GoringWarplow.class, ColossalDreadmaw.class, Recommission.class})
class GoringWarplowTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype cast uses the alternate characteristics and keeps deathtouch")
    void prototypeCastUsesAlternateCharacteristics() {
        harness.setHand(player1, List.of(new GoringWarplow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent warplow = findPermanent(player1, "Goring Warplow");
        assertThat(gqs.getEffectivePower(gd, warplow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warplow)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, warplow)).containsExactly(CardColor.BLACK);
        assertThat(gqs.hasKeyword(gd, warplow, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Normal casting requires six mana and resolves without using prototype")
    void normalCastingRequiresSixMana() {
        harness.setHand(player1, List.of(new GoringWarplow()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Goring Warplow");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goring Warplow");
        harness.assertNotInHand(player1, "Goring Warplow");
    }

    @Test
    @DisplayName("Prototype requires black mana and cannot be paid with only colorless mana")
    void prototypeRequiresBlackMana() {
        harness.setHand(player1, List.of(new GoringWarplow()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Goring Warplow");
        harness.assertNotOnBattlefield(player1, "Goring Warplow");
    }

    @Test
    @DisplayName("A dead prototype resumes its normal mana value and cannot be Recommissioned")
    void deadPrototypeCannotBeRecommissioned() {
        harness.setHand(player1, List.of(new GoringWarplow()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent prototype = findPermanent(player1, "Goring Warplow");
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GoringWarplow());
        prototype.setSummoningSick(false);
        prototype.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        harness.assertInGraveyard(player1, "Goring Warplow");
        harness.assertInGraveyard(player2, "Goring Warplow");
        harness.assertNotOnBattlefield(player1, "Goring Warplow");
        harness.assertNotOnBattlefield(player2, "Goring Warplow");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, prototype.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goring Warplow's deathtouch destroys a larger blocker")
    void deathtouchDestroysLargerBlocker() {
        Permanent warplow = harness.addToBattlefieldAndReturn(player1, new GoringWarplow());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        warplow.setSummoningSick(false);
        warplow.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(warplow.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }
}
