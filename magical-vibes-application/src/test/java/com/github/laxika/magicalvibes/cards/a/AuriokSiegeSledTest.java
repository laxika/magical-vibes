package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.cards.d.DrossGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuriokSiegeSled.class, DrossGolem.class, AuriokGlaivemaster.class, DarksteelBrute.class})
class AuriokSiegeSledTest extends BaseCardTest {

    @Test
    @DisplayName("First ability forces a targeted artifact creature to block")
    void forcesArtifactCreatureToBlock() {
        Permanent sled = addCreatureReady(player1, new AuriokSiegeSled());
        Permanent blocker = addCreatureReady(player2, new DrossGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(sled.getId());

        sled.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Second ability prevents a targeted artifact creature from blocking")
    void preventsArtifactCreatureFromBlocking() {
        Permanent sled = addCreatureReady(player1, new AuriokSiegeSled());
        Permanent blocker = addCreatureReady(player2, new DrossGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        sled.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Both abilities require an artifact creature target")
    void rejectsTargetsThatAreNotArtifactCreatures() {
        addCreatureReady(player1, new AuriokSiegeSled());
        Permanent nonArtifactCreature = addCreatureReady(player2, new AuriokGlaivemaster());
        Permanent nonCreatureArtifact = new Permanent(new DarksteelBrute());
        gd.playerBattlefields.get(player2.getId()).add(nonCreatureArtifact);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonArtifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonArtifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonCreatureArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonCreatureArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Must-block restriction is vacuous when the targeted creature cannot block")
    void mustBlockRestrictionAllowsUnableCreatureToDeclineBlocking() {
        Permanent sled = addCreatureReady(player1, new AuriokSiegeSled());
        Permanent blocker = addCreatureReady(player2, new DrossGolem());
        blocker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        sled.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Can't-block restriction applies only to the targeted source")
    void cantBlockRestrictionIsSourceSpecific() {
        Permanent sled = addCreatureReady(player1, new AuriokSiegeSled());
        Permanent otherSled = addCreatureReady(player1, new AuriokSiegeSled());
        Permanent blocker = addCreatureReady(player2, new DrossGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        sled.setAttacking(true);
        otherSled.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
