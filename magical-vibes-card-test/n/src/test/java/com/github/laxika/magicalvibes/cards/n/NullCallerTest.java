package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Null Caller")
@CardUsed({NullCaller.class, GraspOfDarkness.class})
class NullCallerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and creates a tapped 2/2 black Zombie")
    void createsTappedZombieToken() {
        harness.addToBattlefield(player1, new NullCaller());
        harness.setGraveyard(player1, List.of(new NullCaller()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Null Caller");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Null Caller"));

        harness.assertNotOnBattlefield(player1, "Zombie");
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureInGraveyard() {
        harness.addToBattlefield(player1, new NullCaller());
        harness.setGraveyard(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot exile a creature from an opponent's graveyard")
    void cannotUseOpponentsGraveyard() {
        harness.addToBattlefield(player1, new NullCaller());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new NullCaller()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertInGraveyard(player2, "Null Caller");
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Null Caller can activate repeatedly")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        harness.addToBattlefield(player1, new NullCaller());
        Permanent caller = findPermanent(player1, "Null Caller");
        caller.tap();
        caller.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new NullCaller(), new NullCaller()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Null Caller");
        harness.assertNotOnBattlefield(player1, "Zombie");

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Zombie"))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(caller.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability resolves after Null Caller leaves the battlefield")
    void resolvesAfterSourceDies() {
        harness.addToBattlefield(player1, new NullCaller());
        Permanent caller = findPermanent(player1, "Null Caller");
        harness.setGraveyard(player1, List.of(new NullCaller()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.setHand(player2, List.of(new GraspOfDarkness()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, caller.getId());

        harness.assertNotOnBattlefield(player1, "Null Caller");
        harness.assertInGraveyard(player1, "Null Caller");
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Zombie").isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Four generic mana cannot replace the black mana in the activation cost")
    void requiresBlackMana() {
        harness.addToBattlefield(player1, new NullCaller());
        harness.setGraveyard(player1, List.of(new NullCaller()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> {
            harness.activateAbility(player1, 0, null, null);
            harness.handleGraveyardCardChosen(player1, 0);
        }).isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Null Caller");
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Exiles only the chosen creature from a mixed graveyard")
    void choosesCreatureInMixedGraveyard() {
        harness.addToBattlefield(player1, new NullCaller());
        NullCaller firstCreature = new NullCaller();
        NullCaller chosenCreature = new NullCaller();
        GraspOfDarkness instant = new GraspOfDarkness();
        harness.setGraveyard(player1, List.of(instant, firstCreature, chosenCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 2);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(instant, firstCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenCreature);
        harness.assertNotOnBattlefield(player1, "Zombie");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Zombie"))
                .hasSize(1)
                .allMatch(Permanent::isTapped);
    }
}
