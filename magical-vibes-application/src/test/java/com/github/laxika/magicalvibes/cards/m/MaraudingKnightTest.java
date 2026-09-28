package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.c.CursedFlesh;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PledgeOfLoyalty;
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

@CardUsed({
        ArdentSoldier.class,
        CursedFlesh.class,
        Forest.class,
        MaraudingKnight.class,
        PledgeOfLoyalty.class,
        Plains.class
})
class MaraudingKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Marauding Knight is 2/2 without opponent Plains")
    void baseStatsWithoutOpponentPlains() {
        Permanent knight = addCreatureReady(player1, new MaraudingKnight());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Marauding Knight gets +1/+1 for each opponent Plains")
    void countsOpponentPlains() {
        Permanent knight = addCreatureReady(player1, new MaraudingKnight());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Marauding Knight ignores non-Plains lands")
    void ignoresNonPlainsLands() {
        Permanent knight = addCreatureReady(player1, new MaraudingKnight());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("White creature cannot block Marauding Knight")
    void whiteCreatureCannotBlockMaraudingKnight() {
        Permanent knight = addCreatureReady(player1, new MaraudingKnight());
        knight.setAttacking(true);

        addCreatureReady(player2, new ArdentSoldier());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Marauding Knight takes no combat damage from a white creature")
    void takesNoCombatDamageFromWhiteCreature() {
        Permanent attacker = addCreatureReady(player1, new ArdentSoldier());
        attacker.setAttacking(true);

        Permanent knight = addCreatureReady(player2, new MaraudingKnight());
        knight.setBlocking(true);
        knight.addBlockingTarget(0);

        resolveCombat();

        assertThat(knight.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Ardent Soldier");
        harness.assertInGraveyard(player1, "Ardent Soldier");
        harness.assertOnBattlefield(player2, "Marauding Knight");
    }

    @Test
    @DisplayName("Marauding Knight cannot be targeted by a white Aura")
    void cannotBeTargetedByWhiteAura() {
        Permanent knight = addCreatureReady(player2, new MaraudingKnight());
        harness.setHand(player1, List.of(new PledgeOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Marauding Knight can be targeted by a black Aura")
    void canBeTargetedByBlackAura() {
        Permanent knight = addCreatureReady(player1, new MaraudingKnight());
        harness.setHand(player1, List.of(new CursedFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, knight.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(CursedFlesh.class);
    }
}
