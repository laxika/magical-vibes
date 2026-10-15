package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SidisiRegentOfTheMire.class, BenalishKnight.class, GrizzlyBears.class})
class SidisiRegentOfTheMireTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and returns a creature with one higher mana value")
    void sacrificesCreatureAndReturnsCreatureWithOneHigherManaValue() {
        Permanent sidisi = addReadySidisi();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sidisi);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Knight");
        harness.assertNotInGraveyard(player1, "Benalish Knight");
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value is not one higher")
    void cannotTargetWrongManaValue() {
        addReadySidisi();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice Sidisi itself")
    void cannotSacrificeSidisiItself() {
        addReadySidisi();
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySidisi() {
        return addCreatureReady(player1, new SidisiRegentOfTheMire());
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent sidisi = addReadySidisi();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sidisi.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent sidisi = harness.addToBattlefieldAndReturn(player1, new SidisiRegentOfTheMire());
        sidisi.setSummoningSick(true);
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhenTapped() {
        Permanent sidisi = addReadySidisi();
        sidisi.tap();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        addReadySidisi();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player2, "Benalish Knight");
    }

    @Test
    void abilityStillResolvesAfterSidisiLeavesBattlefield() {
        Permanent sidisi = addReadySidisi();
        addCreatureReady(player1, new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        assertThat(sidisi.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sidisi);
        gd.playerGraveyards.get(player1.getId()).add(sidisi.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Knight");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void removedTargetDoesNotRefundSacrifice() {
        Permanent sidisi = addReadySidisi();
        addCreatureReady(player1, new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Benalish Knight");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(sidisi.isTapped()).isTrue();
    }

    @Test
    void choosesWhichCreatureToSacrificeWhenSeveralAreAvailable() {
        addReadySidisi();
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other).doesNotContain(chosen);
        harness.assertOnBattlefield(player1, "Benalish Knight");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
