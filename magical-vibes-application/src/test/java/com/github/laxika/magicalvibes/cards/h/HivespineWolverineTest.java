package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SealOfStrength;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HivespineWolverine.class, GrizzlyBears.class, Ornithopter.class, SealOfStrength.class})
class HivespineWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode puts a +1/+1 counter on a creature you control")
    void counterMode() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHivespine(0, target.getId());
        resolveCreatureAndEtb();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB mode makes Hivespine Wolverine fight a creature token")
    void fightMode() {
        Permanent token = harness.addToBattlefieldAndReturn(player2, token(new GrizzlyBears()));

        castHivespine(1, token.getId());
        resolveCreatureAndEtb();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent wolverine = findPermanent(player1, "Hivespine Wolverine");
        assertThat(wolverine.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB mode destroys an artifact")
    void destroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castHivespine(2, target.getId());
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("ETB mode destroys an enchantment")
    void destroysEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SealOfStrength());

        castHivespine(2, target.getId());
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Counter mode rejects a creature an opponent controls")
    void counterModeRejectsOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castHivespine(0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Fight mode rejects a nontoken creature")
    void fightModeRejectsNontokenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castHivespine(1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature token");
    }

    @Test
    @DisplayName("Destroy mode rejects a creature target")
    void destroyModeRejectsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castHivespine(2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    private void castHivespine(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new HivespineWolverine()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, mode, targetId);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private com.github.laxika.magicalvibes.model.Card token(com.github.laxika.magicalvibes.model.Card card) {
        card.setToken(true);
        return card;
    }
}
