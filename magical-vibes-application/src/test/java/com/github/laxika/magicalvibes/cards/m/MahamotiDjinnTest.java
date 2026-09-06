package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MahamotiDjinn.class, AirElemental.class, GiantSpider.class, GrizzlyBears.class})
class MahamotiDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mahamoti Djinn puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new MahamotiDjinn(), "{4}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Mahamoti Djinn onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new MahamotiDjinn(), "{4}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mahamoti Djinn");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new MahamotiDjinn()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Mahamoti Djinn enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new MahamotiDjinn(), "{4}{U}{U}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Mahamoti Djinn");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Mahamoti Djinn deals 5 damage to defending player")
    void dealsFiveDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new MahamotiDjinn());
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Mahamoti Djinn cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new MahamotiDjinn());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Mahamoti Djinn can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player2, new AirElemental());
        addCreatureReady(player1, new MahamotiDjinn());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Mahamoti Djinn can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        addCreatureReady(player2, new GiantSpider());
        addCreatureReady(player1, new MahamotiDjinn());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}

