package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosTheMuscle.class, Divination.class, Forest.class, GrizzlyBears.class})
class RakdosTheMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature exiles that many cards from a targeted player's library")
    void sacrificeTriggerExilesCardsAndGrantsAnyManaPlayPermission() {
        addRakdosReady();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Divination exiledSpell = new Divination();
        Forest exiledLand = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(exiledSpell, exiledLand));
        harness.setHand(player1, List.of());

        sacrifice(sacrificed);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledSpell, exiledLand);
        assertThat(gd.exilePlayPermissions.get(exiledSpell.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(exiledSpell.getId(), exiledLand.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, exiledSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(exiledSpell.getId())).isNull();
        harness.assertInGraveyard(player2, "Divination");
    }

    @Test
    @DisplayName("Sacrificing another permanent that is not a creature does not trigger Rakdos")
    void nonCreatureSacrificeDoesNotTrigger() {
        addRakdosReady();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        sacrifice(sacrificed);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activated ability sacrifices another creature, grants indestructible, taps Rakdos, and is once per turn")
    void activatedAbilitySacrificesAndIsOncePerTurn() {
        Permanent rakdos = addRakdosReady();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rakdos.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, rakdos, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing Rakdos itself does not trigger its another-creature ability")
    void sacrificingRakdosItselfDoesNotTrigger() {
        Permanent rakdos = addRakdosReady();
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        sacrifice(rakdos);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exile play permission expires when your next end step begins")
    void playPermissionExpiresAtBeginningOfNextEndStep() {
        addRakdosReady();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest exiled = new Forest();
        harness.setLibrary(player2, List.of(exiled));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        sacrifice(creature);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);
    }

    @Test
    @DisplayName("Rakdos can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent rakdos = harness.addToBattlefieldAndReturn(player1, new RakdosTheMuscle());
        rakdos.tap();
        rakdos.setSummoningSick(true);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rakdos, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(rakdos.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rakdos cannot sacrifice itself to pay its activated ability cost")
    void cannotPayActivatedCostWithRakdosItself() {
        addRakdosReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Rakdos, the Muscle");
    }

    @Test
    @DisplayName("Exiled lands can be played but still obey the land-per-turn limit")
    void exiledLandsRespectLandPlayLimit() {
        addRakdosReady();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        sacrifice(creature);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("A second activation is rejected even when another sacrifice is available")
    void activationLimitAppliesWithAnotherCreatureAvailable() {
        addRakdosReady();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1,
                gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    private Permanent addRakdosReady() {
        return addCreatureReady(player1, new RakdosTheMuscle());
    }

    private void sacrifice(Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), card));
    }
}
