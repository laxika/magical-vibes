package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.c.ChangelingOutcast;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SliverGravemother.class, BonescytheSliver.class, GrizzlyBears.class, Humility.class,
        ChangelingOutcast.class, DoublingSeason.class})
@DisplayName("Sliver Gravemother")
class SliverGravemotherTest extends BaseCardTest {

    @Test
    @DisplayName("Slivers you control survive the legend rule")
    void sliverDuplicatesSurviveLegendRule() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.addToBattlefield(player1, new SliverGravemother());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sliver creature cards in your graveyard gain encore for their mana value")
    void grantsEncoreToSliversInGraveyard() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Bonescythe Sliver");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    void nativeEncoreWorksWithoutAnotherGravemotherOnBattlefield() {
        harness.setGraveyard(player1, List.of(new SliverGravemother()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Sliver Gravemother");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sliver Gravemother"));
        harness.assertNotOnBattlefield(player1, "Sliver Gravemother");
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Sliver Gravemother");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Sliver Gravemother");
    }

    @Test
    void grantedEncoreExilesSourceAsCostAndSacrificesCopyAtNextEndStep() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Bonescythe Sliver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bonescythe Sliver"));
        harness.assertNotOnBattlefield(player1, "Bonescythe Sliver");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bonescythe Sliver");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bonescythe Sliver");
        harness.assertOnBattlefield(player1, "Sliver Gravemother");
    }

    @Test
    void encoreCopyMustAttackIfAble() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedEncoreCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bonescythe Sliver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedEncoreRequiresFullManaValuePayment() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bonescythe Sliver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantEncoreToOpponentsGraveyard() {
        harness.addToBattlefield(player2, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingAbilitiesStopsGrantingEncore() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.addToBattlefield(player1, new Humility());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    void losingAbilitiesRestoresLegendRule() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SliverGravemother());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SliverGravemother());
        harness.addToBattlefield(player1, new Humility());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    void grantsEncoreToChangelingCreatureCards() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new ChangelingOutcast()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Changeling Outcast");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Changeling Outcast");
    }

    @Test
    void doubledEncoreCopiesAreSacrificedByOneDelayedTrigger() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Bonescythe Sliver")).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Encore is not granted to non-Sliver creature cards")
    void doesNotGrantEncoreToNonSlivers() {
        harness.addToBattlefield(player1, new SliverGravemother());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
