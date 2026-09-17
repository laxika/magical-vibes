package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

@CardUsed({SarahsWings.class, GrizzlyBears.class, FountainOfYouth.class})
class SarahsWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target creature flying until end of turn")
    void givesCreatureFlyingUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gives a target player flying, preventing damage from creatures without flying")
    void givesPlayerFlying() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        int lifeBeforeCombat = gd.getLife(player2.getId());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeCombat);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent noncreature = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(noncreature);
        harness.setHand(player1, List.of(new SarahsWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or player");
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player, GrizzlyBears card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
