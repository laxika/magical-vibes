package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.d.DauthiSlayer;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
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

@CardUsed({SoltariMonk.class, DarkBanishing.class, Pacifism.class, DauthiSlayer.class,
        TrainedArmodon.class, SoltariFootSoldier.class})
class SoltariMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent monk = addCreatureReady(player2, new SoltariMonk());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, monk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by a white spell")
    void canBeTargetedByWhiteSpell() {
        Permanent monk = addCreatureReady(player1, new SoltariMonk());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, monk.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Survives combat damage from a black creature")
    void survivesDamageFromBlackCreature() {
        addCreatureReady(player1, new DauthiSlayer());
        Permanent monk = addCreatureReady(player2, new SoltariMonk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(monk.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Soltari Monk");
        harness.assertInGraveyard(player1, "Dauthi Slayer");
    }

    @Test
    @DisplayName("Cannot be blocked by a black creature")
    void cannotBeBlockedByBlackCreature() {
        addCreatureReady(player1, new SoltariMonk());
        addCreatureReady(player2, new DauthiSlayer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be blocked by a creature without shadow")
    void cannotBeBlockedByCreatureWithoutShadow() {
        addCreatureReady(player1, new SoltariMonk());
        addCreatureReady(player2, new TrainedArmodon());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Can be blocked by a creature with shadow")
    void canBeBlockedByCreatureWithShadow() {
        addCreatureReady(player1, new SoltariMonk());
        Permanent blocker = addCreatureReady(player2, new SoltariFootSoldier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
