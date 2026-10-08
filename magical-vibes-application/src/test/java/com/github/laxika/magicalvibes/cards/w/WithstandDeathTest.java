package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.cards.t.TurnToSlag;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WithstandDeath.class, CopperMyr.class, AccordersShield.class, GraspOfDarkness.class, TurnToSlag.class})
class WithstandDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Withstand Death puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Copper Myr");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Withstand Death grants indestructible to target creature")
    void resolvingGrantsIndestructible() {
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Copper Myr");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent myr = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(myr.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Copper Myr");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent myr = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(myr.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Withstand Death fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Copper Myr");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Withstand Death");
    }

    @Test
    @DisplayName("Withstand Death can protect an opponent's creature")
    void protectsOpponentsCreature() {
        harness.addToBattlefield(player2, new CopperMyr());
        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Copper Myr"));

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible prevents destruction from lethal damage")
    void survivesLethalDamage() {
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setHand(player1, List.of(new WithstandDeath(), new TurnToSlag()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 5);
        UUID targetId = harness.getPermanentId(player1, "Copper Myr");

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player1, "Copper Myr");
        harness.assertNotInGraveyard(player1, "Copper Myr");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Indestructible does not prevent death from nonpositive toughness")
    void diesFromToughnessReduction() {
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setHand(player1, List.of(new WithstandDeath(), new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        UUID targetId = harness.getPermanentId(player1, "Copper Myr");

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Copper Myr");
        harness.assertInGraveyard(player1, "Copper Myr");
    }

    @Test
    @DisplayName("Withstand Death cannot target a noncreature artifact")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new AccordersShield());
        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Accorder's Shield");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Withstand Death");
    }
}
