package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaseballBat.class, GrizzlyBears.class})
class BaseballBatTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Baseball Bat attaches it to a target creature you control")
    void enteringAttachesToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BaseballBat()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bat = findPermanent(player1, "Baseball Bat");
        assertThat(bat.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking with the equipped creature taps up to one target creature")
    void attackingTapsTargetCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent bat = addBatReady(player1);
        bat.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                        .validPermanentIds())
                .contains(attacker.getId(), target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger can be declined")
    void attackTriggerCanBeDeclined() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent bat = addBatReady(player1);
        bat.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addBatReady(Player player) {
        Permanent bat = new Permanent(new BaseballBat());
        bat.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bat);
        return bat;
    }
}
