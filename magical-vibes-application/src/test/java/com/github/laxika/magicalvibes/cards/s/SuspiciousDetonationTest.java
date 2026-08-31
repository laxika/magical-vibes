package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HexingSquelcher;
import com.github.laxika.magicalvibes.cards.o.OxiddaDaredevil;
import com.github.laxika.magicalvibes.cards.v.Vizzerdrix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuspiciousDetonation.class, Vizzerdrix.class, Counterspell.class, GrizzlyBears.class,
        HexingSquelcher.class, OxiddaDaredevil.class, Spellbook.class})
class SuspiciousDetonationTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsFourDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Vizzerdrix());
        harness.setHand(player1, List.of(new SuspiciousDetonation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Vizzerdrix");
    }

    @Test
    @DisplayName("Costs 3 less after sacrificing an artifact this turn")
    void costsLessAfterSacrificingArtifact() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Vizzerdrix());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SuspiciousDetonation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without sacrificing an artifact")
    void cannotUseReducedCostWithoutSacrificingArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Vizzerdrix());
        harness.setHand(player1, List.of(new SuspiciousDetonation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered by Counterspell")
    void cannotBeCountered() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SuspiciousDetonation detonation = new SuspiciousDetonation();
        harness.setHand(player1, List.of(detonation));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, detonation.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Counterspell");
    }

    @Test
    @DisplayName("Cannot be countered by ward")
    void cannotBeCounteredByWard() {
        harness.addToBattlefield(player2, new HexingSquelcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuspiciousDetonation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Suspicious Detonation");
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new SuspiciousDetonation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
