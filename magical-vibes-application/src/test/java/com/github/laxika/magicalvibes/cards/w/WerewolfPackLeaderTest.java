package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WerewolfPackLeader.class, GrizzlyBears.class})
class WerewolfPackLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Pack tactics draws a card when attacking creatures have total power at least six")
    void packTacticsDrawsAtThreshold() {
        addCreatureReady(player1, new WerewolfPackLeader());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Pack tactics does not draw when the attacking power is below six")
    void packTacticsRequiresSixPower() {
        addCreatureReady(player1, new WerewolfPackLeader());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Pack tactics requires the Pack Leader to attack")
    void packTacticsRequiresSourceToAttack() {
        addCreatureReady(player1, new WerewolfPackLeader());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The activated ability changes the Pack Leader until end of turn")
    void activatedAbilityChangesPowerTrampleAndSubtype() {
        Permanent leader = addCreatureReady(player1, new WerewolfPackLeader());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, leader)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, leader, Keyword.TRAMPLE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(leader, CardSubtype.HUMAN)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leader)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, leader, Keyword.TRAMPLE)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(leader, CardSubtype.HUMAN)).isTrue();
    }
}
