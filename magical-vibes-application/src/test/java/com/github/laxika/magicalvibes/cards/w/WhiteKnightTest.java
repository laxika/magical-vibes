package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiteKnight.class, GrizzlyBears.class, BogWraith.class, CrawWurm.class,
        Terror.class, SwordsToPlowshares.class, HolyStrength.class, UnholyStrength.class})
class WhiteKnightTest extends BaseCardTest {


    // ===== Casting =====

    @Test
    @DisplayName("Casting White Knight puts it on the stack")
    void castingPutsOnStack() {
        WhiteKnight whiteKnight = new WhiteKnight();
        harness.setHand(player1, List.of(whiteKnight));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(whiteKnight);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts White Knight on the battlefield")
    void resolvingPutsOnBattlefield() {
        WhiteKnight whiteKnight = new WhiteKnight();
        harness.setHand(player1, List.of(whiteKnight));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == whiteKnight);
    }

    // ===== First strike in combat =====

    @Test
    @DisplayName("First strike kills 2/2 blocker before it deals regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        addCreatureReady(player1, new WhiteKnight());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "White Knight");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Black creature cannot block White Knight")
    void blackCreatureCannotBlockWhiteKnight() {
        addCreatureReady(player1, new WhiteKnight());
        addCreatureReady(player2, new BogWraith());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block White Knight")
    void greenCreatureCanBlockWhiteKnight() {
        addCreatureReady(player1, new WhiteKnight());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Protection - combat damage =====

    @Test
    @DisplayName("White Knight takes no combat damage from black creature")
    void takesNoDamageFromBlackCreature() {
        addCreatureReady(player1, new BogWraith());
        addCreatureReady(player2, new WhiteKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Bog Wraith");
        harness.assertOnBattlefield(player2, "White Knight");
    }

    @Test
    @DisplayName("White Knight takes normal combat damage from green creature")
    void takesNormalDamageFromGreenCreature() {
        addCreatureReady(player1, new CrawWurm());
        addCreatureReady(player2, new WhiteKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        // White Knight deals 2 first-strike damage; Craw Wurm survives.
        // Craw Wurm deals 6 regular damage; White Knight dies without protection from green.
        harness.assertOnBattlefield(player1, "Craw Wurm");
        harness.assertNotOnBattlefield(player2, "White Knight");
        harness.assertInGraveyard(player2, "White Knight");
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent knight = addCreatureReady(player2, new WhiteKnight());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, knight.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by white instant")
    void canBeTargetedByWhiteInstant() {
        Permanent knight = addCreatureReady(player1, new WhiteKnight());
        SwordsToPlowshares swordsToPlowshares = new SwordsToPlowshares();
        harness.setHand(player1, List.of(swordsToPlowshares));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, knight.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(swordsToPlowshares);

    }
    // ===== Protection - aura enchantment =====

    @Test
    @DisplayName("Cannot be enchanted by black aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent knight = addCreatureReady(player2, new WhiteKnight());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, knight.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be enchanted by white aura")
    void canBeEnchantedByWhiteAura() {
        Permanent knight = addCreatureReady(player1, new WhiteKnight());
        HolyStrength holyStrength = new HolyStrength();
        harness.setHand(player1, List.of(holyStrength));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, knight.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(holyStrength);
    }
}
