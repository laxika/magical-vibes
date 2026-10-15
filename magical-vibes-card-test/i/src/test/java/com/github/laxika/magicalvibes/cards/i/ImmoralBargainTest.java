package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmoralBargain.class, GrizzlyBears.class, Forest.class, SolRing.class})
class ImmoralBargainTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castWithSacrifices(List<UUID> targetIds, List<UUID> sacrificeIds) {
        if (targetIds.size() <= 1) {
            harness.castSorceryWithSacrifices(player1, 0,
                    targetIds.isEmpty() ? null : targetIds.getFirst(), sacrificeIds);
            return;
        }
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.of(), false, null,
                null, null, null, null, false, null, null, null, sacrificeIds);
    }

    @Test
    @DisplayName("Sacrificing two creatures destroys two target nonland permanents")
    void sacrificesAndDestroysXNonlandPermanents() {
        Permanent sacrificeOne = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sacrificeTwo = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent targetOne = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetTwo = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        castWithSacrifices(List.of(targetOne.getId(), targetTwo.getId()),
                List.of(sacrificeOne.getId(), sacrificeTwo.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing no creatures destroys no permanents")
    void zeroDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        castWithSacrifices(List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature to set X")
    void cannotSacrificeNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(target.getId()), List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(land.getId()), List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void mustChooseExactlyAsManyTargetsAsSacrificedCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(target.getId()),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Sol Ring");
        harness.assertInHand(player1, "Immoral Bargain");
    }

    @Test
    void cannotSacrificeACreatureWithoutChoosingATarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(), List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Immoral Bargain");
    }

    @Test
    void sacrificesArePaidBeforeResolutionAndCanDestroyOwnArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        castWithSacrifices(List.of(target.getId()), List.of(sacrifice.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sol Ring");
        harness.assertInGraveyard(player1, "Sol Ring");
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(target.getId()), List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Sol Ring");
    }

    @Test
    void cannotChooseTheSameTargetTwice() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(target.getId(), target.getId()),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Sol Ring");
    }

    @Test
    void resolvesRemainingTargetWhenAnotherTargetWasSacrificedToCast() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new ImmoralBargain()));
        addMana();

        castWithSacrifices(List.of(first.getId(), target.getId()), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Sol Ring");
        harness.assertInGraveyard(player2, "Sol Ring");
    }
}
