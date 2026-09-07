package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BrokenVisage;
import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.r.RysorianBadger;
import com.github.laxika.magicalvibes.cards.s.SerraBestiary;
import com.github.laxika.magicalvibes.cards.s.SerraPaladin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        IhsansShade.class,
        BeastWalkers.class,
        RysorianBadger.class,
        SerraPaladin.class,
        SerraBestiary.class,
        BrokenVisage.class
})
class IhsansShadeTest extends BaseCardTest {

    @Test
    @DisplayName("White creature cannot block Ihsan's Shade")
    void whiteCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new IhsansShade());
        Permanent blocker = addCreatureReady(player2, new BeastWalkers());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Ihsan's Shade")
    void greenCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new IhsansShade());
        Permanent blocker = addCreatureReady(player2, new RysorianBadger());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Takes no combat damage from a white creature")
    void takesNoDamageFromWhite() {
        Permanent attacker = addCreatureReady(player1, new BeastWalkers());
        Permanent blocker = addCreatureReady(player2, new IhsansShade());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Ihsan's Shade");
    }

    @Test
    @DisplayName("Cannot be targeted by a white ability")
    void cannotBeTargetedByWhiteAbility() {
        Permanent paladin = addCreatureReady(player1, new SerraPaladin());
        Permanent shade = addCreatureReady(player2, new IhsansShade());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(paladin), null, shade.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
        assertThat(paladin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot be enchanted by a white Aura")
    void cannotBeEnchantedByWhiteAura() {
        Permanent shade = addCreatureReady(player2, new IhsansShade());

        harness.setHand(player1, List.of(new SerraBestiary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, shade.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be targeted by a black instant")
    void canBeTargetedByBlackInstant() {
        Permanent shade = addCreatureReady(player1, new IhsansShade());
        shade.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrokenVisage()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, shade.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Broken Visage");
    }
}
