package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.b.BronzeHorse;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.k.KillerBees;
import com.github.laxika.magicalvibes.cards.m.ManaDrain;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Boomerang.class, BronzeHorse.class, DurkwoodBoars.class, KillerBees.class, ManaDrain.class, RemoveSoul.class})
class RemoveSoulTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a creature spell")
    void castingPutsOnStackTargetingCreatureSpell() {
        DurkwoodBoars boars = new DurkwoodBoars();
        harness.castFromHand(player1, boars, "{4}{G}");

        RemoveSoul removeSoul = new RemoveSoul();
        harness.setHand(player2, List.of(removeSoul));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, boars.getId());

        assertThat(gd.stack).hasSize(2);
        StackEntry removeSoulEntry = gd.stack.getLast();
        assertThat(removeSoulEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(removeSoulEntry.getCard()).isSameAs(removeSoul);
        assertThat(removeSoulEntry.getTargetId()).isEqualTo(boars.getId());
    }

    @Test
    @DisplayName("Can target an artifact creature spell")
    void canTargetArtifactCreatureSpell() {
        BronzeHorse horse = new BronzeHorse();
        harness.castFromHand(player1, horse, "{7}");

        harness.setHand(player2, List.of(new RemoveSoul()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, horse.getId());

        harness.assertInGraveyard(player1, "Bronze Horse");
        harness.assertNotOnBattlefield(player1, "Bronze Horse");
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        harness.addToBattlefield(player1, new DurkwoodBoars());

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        RemoveSoul removeSoul = new RemoveSoul();
        harness.setHand(player2, List.of(removeSoul));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Durkwood Boars"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, boomerang.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature spell");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removeSoul);
    }

    @Test
    @DisplayName("Resolving counters a creature spell")
    void countersCreatureSpell() {
        DurkwoodBoars boars = new DurkwoodBoars();
        harness.castFromHand(player1, boars, "{4}{G}");

        harness.setHand(player2, List.of(new RemoveSoul()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, boars.getId());

        // Countered spell goes to owner's graveyard
        harness.assertInGraveyard(player1, "Durkwood Boars");
        // Does not enter the battlefield
        harness.assertNotOnBattlefield(player1, "Durkwood Boars");
    }

    @Test
    @DisplayName("Remove Soul goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        DurkwoodBoars boars = new DurkwoodBoars();
        harness.castFromHand(player1, boars, "{4}{G}");

        harness.setHand(player2, List.of(new RemoveSoul()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, boars.getId());

        harness.assertInGraveyard(player2, "Remove Soul");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        DurkwoodBoars boars = new DurkwoodBoars();
        harness.castFromHand(player1, boars, "{4}{G}");

        harness.setHand(player2, List.of(new RemoveSoul()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, boars.getId());

        ManaDrain manaDrain = new ManaDrain();
        harness.setHand(player1, List.of(manaDrain));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, boars.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Remove Soul still goes to graveyard
        harness.assertInGraveyard(player2, "Remove Soul");
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        KillerBees bees = new KillerBees();
        addCreatureReady(player1, bees);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);

        RemoveSoul removeSoul = new RemoveSoul();
        harness.setHand(player2, List.of(removeSoul));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bees.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removeSoul);
    }

    @Test
    @DisplayName("Can target its controller's creature spell")
    void canTargetItsControllersCreatureSpell() {
        DurkwoodBoars boars = new DurkwoodBoars();
        harness.castFromHand(player1, boars, "{4}{G}");

        RemoveSoul removeSoul = new RemoveSoul();
        harness.setHand(player1, List.of(removeSoul));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, boars.getId());

        harness.assertInGraveyard(player1, "Durkwood Boars");
        harness.assertInGraveyard(player1, "Remove Soul");
        assertThat(gd.stack).isEmpty();
    }
}
