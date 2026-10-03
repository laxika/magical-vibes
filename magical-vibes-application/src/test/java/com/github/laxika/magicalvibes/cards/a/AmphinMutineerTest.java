package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AmphinMutineer.class, GrizzlyBears.class, SolemnSimulacrum.class})
class AmphinMutineerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters exiling a non-Salamander creature and gives its controller a Salamander Warrior")
    void entersExilesCreatureAndCreatesSalamanderWarrior() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AmphinMutineer()));
        addManaForCast();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player2, "Salamander Warrior"))
                .anyMatch(token -> token.getCard().isToken()
                        && token.getCard().hasType(CardType.CREATURE)
                        && token.getCard().getColor() == CardColor.BLUE
                        && token.getCard().getPower() == 4
                        && token.getCard().getToughness() == 3
                        && token.getCard().getSubtypes().contains(CardSubtype.SALAMANDER)
                        && token.getCard().getSubtypes().contains(CardSubtype.WARRIOR));
    }

    @Test
    @DisplayName("Cannot target a Salamander creature")
    void cannotTargetSalamanderCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AmphinMutineer());
        harness.setHand(player1, List.of(new AmphinMutineer()));
        addManaForCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Salamander creature");
    }

    @Test
    @DisplayName("Encore creates an untapped hasty token and sacrifices it at the next end step")
    void encoreCreatesAndSacrificesToken() {
        harness.setGraveyard(player1, List.of(new AmphinMutineer()));
        addManaForEncore();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Amphin Mutineer");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Amphin Mutineer")).isEmpty();
    }

    @Test
    @DisplayName("Choosing no ETB target leaves an available creature alone and creates no token")
    void mayChooseNoTarget() {
        harness.addToBattlefield(player2, new SolemnSimulacrum());

        harness.castFromHand(player1, new AmphinMutineer(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Amphin Mutineer");
        harness.assertOnBattlefield(player2, "Solemn Simulacrum");
        assertThat(findPermanents(player1, "Salamander Warrior")).isEmpty();
        assertThat(findPermanents(player2, "Salamander Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Exiling your own creature gives you the replacement token")
    void canExileOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolemnSimulacrum());
        harness.setHand(player1, List.of(new AmphinMutineer()));
        addManaForCast();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Solemn Simulacrum");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Solemn Simulacrum"));
        assertThat(findPermanents(player1, "Salamander Warrior")).hasSize(1);
        assertThat(findPermanents(player2, "Salamander Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Encore exiles its source as a cost before any token is created")
    void encoreExilesSourceOnActivation() {
        harness.setGraveyard(player1, List.of(new AmphinMutineer()));
        addManaForEncore();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Amphin Mutineer"));
        assertThat(findPermanents(player1, "Amphin Mutineer")).isEmpty();

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Amphin Mutineer")).hasSize(1);
    }

    @Test
    @DisplayName("Encore cannot be activated during upkeep")
    void encoreRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new AmphinMutineer()));
        addManaForEncore();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Encore copy triggers the copied exile ability")
    void encoreCopyExilesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        harness.setGraveyard(player1, List.of(new AmphinMutineer()));
        addManaForEncore();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Amphin Mutineer")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Solemn Simulacrum");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Solemn Simulacrum"));
        assertThat(findPermanents(player2, "Salamander Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Encore sacrifice uses the stack and leaves a response window at the end step")
    void encoreSacrificeCanBeRespondedTo() {
        harness.setGraveyard(player1, List.of(new AmphinMutineer()));
        addManaForEncore();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Amphin Mutineer")).hasSize(1);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Amphin Mutineer")).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Amphin Mutineer")).isEmpty();
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
