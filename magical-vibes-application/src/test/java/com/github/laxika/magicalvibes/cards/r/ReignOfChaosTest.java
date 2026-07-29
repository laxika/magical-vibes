package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReignOfChaosTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Mode 0 destroys target Plains and target white creature")
    void mode0DestroysPlainsAndWhiteCreature() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castModalInstant(player1, 0, 0, List.of(plains.getId(), vanguard.getId()));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).extracting(Permanent::getId)
                .doesNotContain(plains.getId(), vanguard.getId())
                .contains(bears.getId());
    }

    @Test
    @DisplayName("Mode 1 destroys target Island and target blue creature")
    void mode1DestroysIslandAndBlueCreature() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.castModalInstant(player1, 0, 1, List.of(island.getId(), wizard.getId()));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).extracting(Permanent::getId)
                .doesNotContain(island.getId(), wizard.getId())
                .contains(plains.getId());
    }

    @Test
    @DisplayName("Mode 0 cannot target a non-white creature")
    void mode0RejectsNonWhiteCreature() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        UUID plainsId = plains.getId();
        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(plainsId, bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 1 cannot target a Plains as its land target")
    void mode1RejectsPlains() {
        harness.setHand(player1, List.of(new ReignOfChaos()));
        giveMana();
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());

        UUID plainsId = plains.getId();
        UUID wizardId = wizard.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(plainsId, wizardId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
