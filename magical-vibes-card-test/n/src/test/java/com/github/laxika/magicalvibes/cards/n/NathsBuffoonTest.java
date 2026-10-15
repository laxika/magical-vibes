package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.cards.e.EyeblightsEnding;
import com.github.laxika.magicalvibes.cards.k.KithkinDaggerdare;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.l.LowlandOaf;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
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

@CardUsed({NathsBuffoon.class, ElvishEulogist.class, LowlandOaf.class, KithkinDaggerdare.class,
        WoodlandChangeling.class, EyeblightsEnding.class, NamelessInversion.class, Tarfire.class, Lignify.class})
class NathsBuffoonTest extends BaseCardTest {

    @Test
    @DisplayName("Nath's Buffoon takes no combat damage from Elf creature when blocking")
    void takesNoDamageFromElf() {
        Permanent attacker = addCreatureReady(player1, new ElvishEulogist());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new NathsBuffoon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Elvish Eulogist");
        harness.assertOnBattlefield(player2, "Nath's Buffoon");
    }

    @Test
    @DisplayName("Nath's Buffoon takes normal combat damage from non-Elf creature when blocking")
    void takesNormalDamageFromNonElf() {
        Permanent attacker = addCreatureReady(player1, new LowlandOaf());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new NathsBuffoon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Nath's Buffoon");
    }

    @Test
    @DisplayName("Elf creature cannot block Nath's Buffoon")
    void elfCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new NathsBuffoon());
        attacker.setAttacking(true);

        addCreatureReady(player2, new ElvishEulogist());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Elf creature can block Nath's Buffoon")
    void nonElfCanBlock() {
        Permanent attacker = addCreatureReady(player1, new NathsBuffoon());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new KithkinDaggerdare());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void changelingCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new NathsBuffoon());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WoodlandChangeling());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void changelingCombatDamageIsPrevented() {
        Permanent attacker = addCreatureReady(player1, new WoodlandChangeling());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NathsBuffoon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Woodland Changeling");
        harness.assertOnBattlefield(player2, "Nath's Buffoon");
    }

    @Test
    void elfInstantCannotTarget() {
        Permanent buffoon = harness.addToBattlefieldAndReturn(player2, new NathsBuffoon());
        harness.setHand(player1, List.of(new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, buffoon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void changelingInstantCannotTargetEvenForItsController() {
        Permanent buffoon = harness.addToBattlefieldAndReturn(player1, new NathsBuffoon());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, buffoon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void nonElfKindredInstantCanTargetAndDealDamage() {
        Permanent buffoon = harness.addToBattlefieldAndReturn(player2, new NathsBuffoon());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, buffoon.getId());

        harness.assertInGraveyard(player2, "Nath's Buffoon");
    }

    @Test
    void losingAllAbilitiesAllowsElfSpellToTarget() {
        Permanent buffoon = harness.addToBattlefieldAndReturn(player2, new NathsBuffoon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Lignify(), new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, buffoon.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lignify");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, buffoon.getId());

        harness.assertInGraveyard(player2, "Nath's Buffoon");
    }

    @Test
    void creatureThatLostElfSubtypeCanBlock() {
        Permanent buffoon = addCreatureReady(player1, new NathsBuffoon());
        Permanent eulogist = addCreatureReady(player2, new ElvishEulogist());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, eulogist.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lignify");
        buffoon.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(eulogist.isBlocking()).isTrue();
    }
}
