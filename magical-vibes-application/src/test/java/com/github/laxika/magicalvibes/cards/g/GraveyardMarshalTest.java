package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@DisplayName("Graveyard Marshal")
@CardUsed({GraveyardMarshal.class, WalkingCorpse.class, Shock.class})
class GraveyardMarshalTest extends BaseCardTest {

    private int setUpBoard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent marshal = addCreatureReady(player1, new GraveyardMarshal());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        return gd.playerBattlefields.get(player1.getId()).indexOf(marshal);
    }

    @Test
    @DisplayName("Exiles the chosen creature card and creates a tapped 2/2 black Zombie")
    void createsTappedZombieToken() {
        int idx = setUpBoard();
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));

        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertNotOnBattlefield(player1, "Zombie");

        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Zombie");
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureInGraveyard() {
        int idx = setUpBoard();
        harness.setGraveyard(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use a creature in an opponent's graveyard to pay the cost")
    void cannotExileOpponentsCreature() {
        int idx = setUpBoard();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new WalkingCorpse()));

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Cannot choose a noncreature when a creature is available")
    void rejectsNoncreatureSelection() {
        int idx = setUpBoard();
        harness.setGraveyard(player1, List.of(new Shock(), new WalkingCorpse()));

        harness.activateAbility(player1, idx, null, null);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("A tapped summoning-sick Marshal can activate repeatedly with separate costs")
    void tappedSummoningSickMarshalCanActivateRepeatedly() {
        int idx = setUpBoard();
        Permanent marshal = findPermanent(player1, "Graveyard Marshal");
        marshal.setSummoningSick(true);
        marshal.tap();
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Zombie")).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(marshal.isTapped()).isTrue();
    }
}
