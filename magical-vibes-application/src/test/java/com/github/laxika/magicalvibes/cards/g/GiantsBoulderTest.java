package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantsBoulder.class, GrizzlyBears.class})
class GiantsBoulderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield scries two cards")
    void enteringBattlefieldScriesTwo() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, new GiantsBoulder(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Paying one mana lets it produce a chosen color")
    void producesChosenColor() {
        Permanent boulder = harness.addToBattlefieldAndReturn(player1, new GiantsBoulder());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(boulder.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying seven mana and sacrificing it destroys target permanent")
    void sacrificesToDestroyTargetPermanent() {
        Permanent boulder = harness.addToBattlefieldAndReturn(player1, new GiantsBoulder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(boulder);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(boulder.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("The destruction ability rejects a player as its target")
    void rejectsPlayerTarget() {
        Permanent boulder = harness.addToBattlefieldAndReturn(player1, new GiantsBoulder());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(boulder);
    }

    @Test
    void sacrificeIsPaidBeforeDestroyingAnArtifact() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GiantsBoulder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantsBoulder());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void canTargetItselfAndIsStillSacrificed() {
        Permanent boulder = harness.addToBattlefieldAndReturn(player1, new GiantsBoulder());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, boulder.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(boulder);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(boulder.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(boulder.getCard());
    }

    @Test
    void tappedBoulderCannotActivateDestructionAbility() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GiantsBoulder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantsBoulder());
        source.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotProduceManaWithoutPayingOneMana() {
        Permanent boulder = harness.addToBattlefieldAndReturn(player1, new GiantsBoulder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(boulder.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
