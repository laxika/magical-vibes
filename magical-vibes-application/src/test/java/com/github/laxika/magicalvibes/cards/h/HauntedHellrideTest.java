package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HauntedHellrideTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, a creature you control gets boosted, gains deathtouch, and untaps")
    void boostsGivesDeathtouchAndUntapsTargetYouControl() {
        addVehicleReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(attacker.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = new Permanent(new HauntedHellride());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
