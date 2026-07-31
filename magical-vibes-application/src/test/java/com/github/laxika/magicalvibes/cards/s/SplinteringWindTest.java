package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SplinteringWindTest extends BaseCardTest {

    private Permanent splinterToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Splinter".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }

    private Permanent activateOnTarget(Permanent target) {
        harness.addToBattlefield(player1, new SplinteringWind());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        return splinterToken();
    }

    @Test
    @DisplayName("Ability deals 1 damage to target creature and creates a 1/1 flying Splinter")
    void damagesTargetAndCreatesToken() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent token = activateOnTarget(bears);

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Declining the Splinter's cumulative upkeep sacrifices it, dealing 1 damage to its controller and each creature they control")
    void unpaidUpkeepSacrificesAndDamages() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent token = activateOnTarget(bears);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(ownBears.getMarkedDamage()).isEqualTo(1);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }
}
