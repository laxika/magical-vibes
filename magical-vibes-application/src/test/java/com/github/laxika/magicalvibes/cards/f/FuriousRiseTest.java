package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkysovereignConsulFlagship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuriousRise.class, AvatarOfMight.class, GrizzlyBears.class, Forest.class,
        SkysovereignConsulFlagship.class})
class FuriousRiseTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card and grants play permission with a power-4-or-greater creature")
    void exilesTopCardAndGrantsPermission() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addRiseWithBigCreature();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("The next trigger revokes the previous card's permission")
    void nextTriggerRevokesPreviousPermission() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        addRiseWithBigCreature();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsEntry(firstCard.getId(), player1.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).doesNotContainKey(firstCard.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(secondCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerWithoutBigCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new FuriousRise());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The permission remains after Furious Rise leaves the battlefield")
    void permissionRemainsAfterSourceLeaves() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent rise = addRiseWithBigCreature();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(rise);

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    private Permanent addRiseWithBigCreature() {
        Permanent rise = harness.addToBattlefieldAndReturn(player1, new FuriousRise());
        harness.addToBattlefield(player1, new AvatarOfMight());
        return rise;
    }

    @Test
    void uncrewedVehicleDoesNotSatisfyCreatureCondition() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new FuriousRise());
        harness.addToBattlefield(player1, new SkysovereignConsulFlagship());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void conditionIsCheckedAgainOnResolution() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new FuriousRise());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void emptyLibraryDoesNotRevokePreviousPermission() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addRiseWithBigCreature();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void onlyControllersEndStepTriggers() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addRiseWithBigCreature();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void separateEnchantmentsKeepIndependentPermissions() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        addRiseWithBigCreature();
        harness.addToBattlefield(player1, new FuriousRise());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).containsEntry(firstCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(secondCard.getId(), player1.getId());
    }

    @Test
    void exiledCreatureCanBeCastOnLaterMainPhaseByPayingItsCost() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addRiseWithBigCreature();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void permissionDoesNotAllowCreatureCastingDuringEndStep() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addRiseWithBigCreature();
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void exiledLandCanBePlayedDuringMainPhase() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addRiseWithBigCreature();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
