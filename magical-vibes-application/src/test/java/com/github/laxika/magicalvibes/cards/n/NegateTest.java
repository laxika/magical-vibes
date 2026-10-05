package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Negate.class, LlanowarElves.class, MightOfOaks.class, HowlingMine.class, Ornithopter.class, RodOfRuin.class})
class NegateTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a noncreature spell")
    void castingPutsOnStackTargetingNoncreatureSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry negateEntry = gd.stack.getLast();
        assertThat(negateEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(negateEntry.getCard().getName()).isEqualTo("Negate");
        assertThat(negateEntry.getTargetId()).isEqualTo(might.getId());
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters a noncreature spell")
    void countersNoncreatureSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        // Countered spell goes to owner's graveyard
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @DisplayName("Negate goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Negate");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        // Remove target from stack before Negate resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Might of Oaks"));

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        // Negate still goes to graveyard
        harness.assertInGraveyard(player2, "Negate");
    }

    @Test
    @DisplayName("Counters a noncreature artifact spell")
    void countersArtifactSpell() {
        HowlingMine mine = new HowlingMine();
        harness.setHand(player1, List.of(mine));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mine.getId());

        harness.assertInGraveyard(player1, "Howling Mine");
        harness.assertNotOnBattlefield(player1, "Howling Mine");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an artifact creature spell")
    void cannotTargetArtifactCreatureSpell() {
        Ornithopter thopter = new Ornithopter();
        harness.setHand(player1, List.of(thopter));
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, thopter.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Negate");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, abilityId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Negate");
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can counter a spell controlled by its caster")
    void countersOwnSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might, new Negate()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, might.getId());

        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertInGraveyard(player1, "Negate");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
