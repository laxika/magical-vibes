package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SunCrestedPterodon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelentlessRaptor.class, RaptorCompanion.class, SunCrestedPterodon.class})
class RelentlessRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Relentless Raptor must attack each combat if able")
    void mustAttackEachCombat() {
        addCreatureReady(player1, new RelentlessRaptor());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Relentless Raptor must block each combat if able")
    void mustBlockEachCombat() {
        addCreatureReady(player2, new RelentlessRaptor());
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Relentless Raptor satisfies its attack requirement when attacking")
    void attackingSatisfiesRequirement() {
        addCreatureReady(player1, new RelentlessRaptor());
        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Relentless Raptor satisfies its block requirement when blocking")
    void blockingSatisfiesRequirement() {
        addCreatureReady(player2, new RelentlessRaptor());
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers(player1);
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void tappedRaptorIsNotRequiredToAttack() {
        addCreatureReady(player1, new RelentlessRaptor()).setTapped(true);

        assertThatCode(() -> declareAttackers(player1, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void summoningSickRaptorIsNotRequiredToAttack() {
        addCreatureReady(player1, new RelentlessRaptor()).setSummoningSick(true);

        assertThatCode(() -> declareAttackers(player1, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void tappedRaptorIsNotRequiredToBlock() {
        addCreatureReady(player2, new RelentlessRaptor()).setTapped(true);
        addCreatureReady(player1, new RaptorCompanion());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void raptorIsNotRequiredToBlockAnAttackerItCannotBlock() {
        addCreatureReady(player2, new RelentlessRaptor());
        addCreatureReady(player1, new SunCrestedPterodon());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void summoningSickRaptorStillMustBlock() {
        addCreatureReady(player2, new RelentlessRaptor()).setSummoningSick(true);
        addCreatureReady(player1, new RaptorCompanion());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }
}
