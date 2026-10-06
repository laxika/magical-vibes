package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrusivePackbeast.class, VernadiShieldmate.class, SelesnyaGuildgate.class})
class IntrusivePackbeastTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps two target creatures an opponent controls")
    void tapsTwoTargetCreatures() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());

        castPackbeast(List.of(bear1.getId(), bear2.getId()));

        assertThat(bear1.isTapped()).isTrue();
        assertThat(bear2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can tap one target creature")
    void tapsOneTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());

        castPackbeast(List.of(bear.getId()));

        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can choose no targets")
    void canChooseNoTargets() {
        castPackbeast(List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Intrusive Packbeast");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new IntrusivePackbeast()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new SelesnyaGuildgate());
        harness.setHand(player1, List.of(new IntrusivePackbeast()));
        addMana();

        UUID islandId = island.getId();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(islandId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose no targets even when opposing creatures are available")
    void canDeclineAvailableTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());

        castPackbeast(List.of());

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Intrusive Packbeast");
    }

    @Test
    @DisplayName("Already tapped creatures are legal targets")
    void canTargetAlreadyTappedCreature() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        tapped.tap();

        castPackbeast(List.of(tapped.getId(), untapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot choose more than two targets")
    void cannotTargetThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new IntrusivePackbeast()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Remaining target is tapped when the other leaves before the trigger resolves")
    void resolvesForRemainingLegalTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new IntrusivePackbeast()));
        addMana();
        harness.castCreature(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        gd.playerGraveyards.get(player2.getId()).add(removed.getCard());

        resolveAllTriggers();

        assertThat(remaining.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Intrusive Packbeast");
    }

    private void castPackbeast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new IntrusivePackbeast()));
        addMana();

        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
