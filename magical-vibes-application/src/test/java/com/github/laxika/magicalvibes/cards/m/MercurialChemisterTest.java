package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercurialChemisterTest extends BaseCardTest {

    @Test
    @DisplayName("{U}, {T}: Draw two cards")
    void blueAbilityDrawsTwoCards() {
        addReadyChemister(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Ornithopter(), new FountainOfYouth()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        Permanent chemister = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chemister.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{R}, {T}, Discard a card: deals damage equal to discarded mana value")
    void redAbilityDealsDiscardedManaValueDamage() {
        addReadyChemister(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears())); // MV 2
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        Permanent chemister = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chemister.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Discarding a mana-value-0 card deals no damage")
    void zeroManaValueDealsNoDamage() {
        addReadyChemister(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ornithopter())); // MV 0
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(0);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot activate red ability with empty hand")
    void cannotActivateRedWithoutDiscard() {
        addReadyChemister(player1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Red ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyChemister(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addReadyChemister(Player player) {
        Permanent perm = new Permanent(new MercurialChemister());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
