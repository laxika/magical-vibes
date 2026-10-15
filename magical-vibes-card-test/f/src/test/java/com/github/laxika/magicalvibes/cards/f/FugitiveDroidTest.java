package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FugitiveDroid.class, GrizzlyBears.class, Shatter.class, Shock.class, Spellbook.class})
class FugitiveDroidTest extends BaseCardTest {

    @Test
    @DisplayName("Can be blocked until an artifact enters under its controller's control")
    void becomesUnblockableAfterArtifactEnters() {
        Permanent droid = harness.addToBattlefieldAndReturn(player1, new FugitiveDroid());

        assertThat(gqs.hasCantBeBlocked(gd, droid)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new Spellbook());

        assertThat(gqs.hasCantBeBlocked(gd, droid)).isTrue();
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not enable unblockability")
    void opponentArtifactDoesNotEnableUnblockability() {
        Permanent droid = harness.addToBattlefieldAndReturn(player1, new FugitiveDroid());

        harness.enterBattlefieldAndReturn(player2, new Spellbook());

        assertThat(gqs.hasCantBeBlocked(gd, droid)).isFalse();
    }

    @Test
    @DisplayName("The Droid's own entry enables its unblockability")
    void ownEntryEnablesUnblockability() {
        Permanent droid = harness.enterBattlefieldAndReturn(player1, new FugitiveDroid());

        assertThat(gqs.hasCantBeBlocked(gd, droid)).isTrue();
    }

    @Test
    @DisplayName("A nonartifact creature entering does not enable unblockability")
    void nonartifactEntryDoesNotEnableUnblockability() {
        Permanent droid = harness.addToBattlefieldAndReturn(player1, new FugitiveDroid());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasCantBeBlocked(gd, droid)).isFalse();
    }

    @Test
    @DisplayName("Unblockability ends when the next turn begins")
    void unblockabilityEndsNextTurn() {
        Permanent droid = harness.enterBattlefieldAndReturn(player1, new FugitiveDroid());
        assertThat(gqs.hasCantBeBlocked(gd, droid)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasCantBeBlocked(gd, droid)).isFalse();
    }

    @Test
    @DisplayName("Counters a spell targeting an artifact or creature you control")
    void countersSpellTargetingArtifactOrCreatureYouControl() {
        harness.addToBattlefield(player1, new FugitiveDroid());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Fugitive Droid");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a spell targeting an artifact you control")
    void countersSpellTargetingArtifactYouControl() {
        harness.addToBattlefield(player1, new FugitiveDroid());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Shatter shatter = new Shatter();
        harness.setHand(player2, List.of(shatter));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, spellbook.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, shatter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shatter");
        harness.assertInGraveyard(player1, "Fugitive Droid");
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Cannot target a spell that does not target an artifact or creature you control")
    void cannotTargetUnrelatedSpell() {
        harness.addToBattlefield(player1, new FugitiveDroid());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player2.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot counter a spell targeting only an opponent's creature")
    void cannotTargetSpellTargetingOpponentCreature() {
        harness.addToBattlefield(player1, new FugitiveDroid());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Fugitive Droid");
    }

    @Test
    @DisplayName("The entering artifact need not remain on the battlefield")
    void artifactLeavingDoesNotEndUnblockability() {
        Permanent droid = harness.addToBattlefieldAndReturn(player1, new FugitiveDroid());
        Permanent spellbook = harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, spellbook.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gqs.hasCantBeBlocked(gd, droid)).isTrue();
    }

    @Test
    @DisplayName("Can counter its controller's spell and pays sacrifice before resolution")
    void countersOwnSpellAndSacrificesAsCost() {
        harness.addToBattlefield(player1, new FugitiveDroid());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.activateAbility(player1, 0, null, shock.getId());

        harness.assertInGraveyard(player1, "Fugitive Droid");
        harness.assertNotInGraveyard(player1, "Shock");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
