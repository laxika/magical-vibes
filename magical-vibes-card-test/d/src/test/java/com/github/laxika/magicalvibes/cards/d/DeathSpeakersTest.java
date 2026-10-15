package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AysenBureaucrats;
import com.github.laxika.magicalvibes.cards.b.BrokenVisage;
import com.github.laxika.magicalvibes.cards.c.Carapace;
import com.github.laxika.magicalvibes.cards.g.GrandmotherSengir;
import com.github.laxika.magicalvibes.cards.s.Shrink;
import com.github.laxika.magicalvibes.cards.t.Torture;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({DeathSpeakers.class, AysenBureaucrats.class, BrokenVisage.class, Carapace.class,
        DrySpell.class, DwarvenTrader.class, GrandmotherSengir.class, Shrink.class, Torture.class,
        WrathOfGod.class})
class DeathSpeakersTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature cannot block Death Speakers")
    void blackCreatureCannotBlock() {
        addCreatureReady(player1, new DeathSpeakers());
        addCreatureReady(player2, new GrandmotherSengir());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Nonblack creature can block Death Speakers")
    void nonBlackCreatureCanBlock() {
        addCreatureReady(player1, new DeathSpeakers());
        Permanent blocker = addCreatureReady(player2, new DwarvenTrader());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Takes no combat damage from black creature")
    void takesNoDamageFromBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new GrandmotherSengir());
        Permanent blocker = addCreatureReady(player2, new DeathSpeakers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent speakers = addCreatureReady(player2, new DeathSpeakers());
        speakers.setAttacking(true);

        harness.setHand(player1, List.of(new BrokenVisage()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, speakers.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by a nonblack spell")
    void canBeTargetedByNonBlackSpell() {
        Permanent speakers = addCreatureReady(player1, new DeathSpeakers());

        harness.setHand(player1, List.of(new Shrink()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, speakers.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(speakers.getId());
    }

    @Test
    @DisplayName("Cannot be targeted by a black ability")
    void cannotBeTargetedByBlackAbility() {
        addCreatureReady(player1, new GrandmotherSengir());
        Permanent speakers = addCreatureReady(player2, new DeathSpeakers());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, speakers.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by a nonblack ability")
    void canBeTargetedByNonBlackAbility() {
        addCreatureReady(player1, new AysenBureaucrats());
        Permanent speakers = addCreatureReady(player2, new DeathSpeakers());

        harness.activateAbility(player1, 0, null, speakers.getId());
        harness.passBothPriorities();

        assertThat(speakers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot be enchanted by a black Aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent speakers = addCreatureReady(player2, new DeathSpeakers());

        harness.setHand(player1, List.of(new Torture()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, speakers.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be enchanted by a nonblack Aura")
    void canBeEnchantedByNonBlackAura() {
        Permanent speakers = addCreatureReady(player1, new DeathSpeakers());

        harness.setHand(player1, List.of(new Carapace()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, speakers.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(speakers.getId()));
    }

    @Test
    @DisplayName("Takes no noncombat damage from a black spell")
    void takesNoNoncombatDamageFromBlackSpell() {
        Permanent speakers = addCreatureReady(player2, new DeathSpeakers());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new DrySpell()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(speakers.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Death Speakers");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Protection does not prevent combat damage from a nonblack creature")
    void takesCombatDamageFromNonBlackCreature() {
        addCreatureReady(player1, new DwarvenTrader());
        addCreatureReady(player2, new DeathSpeakers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Dwarven Trader");
        harness.assertInGraveyard(player2, "Death Speakers");
        harness.assertNotOnBattlefield(player2, "Death Speakers");
    }

    @Test
    @DisplayName("An attached black Aura is put into the graveyard by state-based actions")
    void attachedBlackAuraIsRemoved() {
        Permanent speakers = addCreatureReady(player1, new DeathSpeakers());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Torture());
        aura.setAttachedTo(speakers.getId());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Death Speakers");
        harness.assertNotOnBattlefield(player2, "Torture");
        harness.assertInGraveyard(player2, "Torture");
    }

    @Test
    @DisplayName("Protection does not prevent untargeted destruction")
    void untargetedDestructionStillDestroysCreature() {
        harness.addToBattlefield(player2, new DeathSpeakers());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Death Speakers");
        harness.assertInGraveyard(player2, "Death Speakers");
    }
}
