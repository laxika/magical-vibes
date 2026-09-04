package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalruumPiper.class, CloudElemental.class, DarajaGriffin.class, PantherWarriors.class})
class TalruumPiperTest extends BaseCardTest {

    @Test
    @DisplayName("Flying creatures must block Talruum Piper; ground creatures are not forced")
    void flyingMustBlockGroundNotForced() {
        Permanent piper = addCreatureReady(player1, new TalruumPiper());
        piper.setAttacking(true);

        Permanent flyer = addCreatureReady(player2, new DarajaGriffin());
        Permanent ground = addCreatureReady(player2, new PantherWarriors());

        prepareDeclareBlockers();

        // Empty blocks — flyer must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Only ground blocks — flyer still must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Flyer alone — ground is not forced
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
        assertThat(ground.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("No blockers required when defender has only ground creatures")
    void onlyGroundCreaturesEmptyBlocksOk() {
        Permanent piper = addCreatureReady(player1, new TalruumPiper());
        piper.setAttacking(true);

        Permanent firstGround = addCreatureReady(player2, new PantherWarriors());
        Permanent secondGround = addCreatureReady(player2, new PantherWarriors());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(firstGround.isBlocking()).isFalse();
        assertThat(secondGround.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("All able flying creatures must block Talruum Piper")
    void twoFlyersMustBothBlock() {
        Permanent piper = addCreatureReady(player1, new TalruumPiper());
        piper.setAttacking(true);

        Permanent firstFlyer = addCreatureReady(player2, new DarajaGriffin());
        Permanent secondFlyer = addCreatureReady(player2, new DarajaGriffin());

        prepareDeclareBlockers();

        // Only one flyer assigned — should fail because both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Both flyers assigned — should succeed
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(firstFlyer.isBlocking()).isTrue();
        assertThat(secondFlyer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped flying creatures are not required to block Talruum Piper")
    void tappedFlyingCreatureIsNotForcedToBlock() {
        Permanent piper = addCreatureReady(player1, new TalruumPiper());
        piper.setAttacking(true);

        Permanent tappedFlyer = addCreatureReady(player2, new DarajaGriffin());
        tappedFlyer.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(tappedFlyer.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A flying creature unable to block Talruum Piper is not required to block")
    void flyingCreatureUnableToBlockPiperIsNotForcedToBlock() {
        Permanent piper = addCreatureReady(player1, new TalruumPiper());
        piper.setAttacking(true);

        Permanent restrictedFlyer = addCreatureReady(player2, new CloudElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(restrictedFlyer.isBlocking()).isFalse();
    }
}
