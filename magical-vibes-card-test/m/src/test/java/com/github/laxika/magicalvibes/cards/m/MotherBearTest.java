package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed(MotherBear.class)
class MotherBearTest extends BaseCardTest {

    @Test
    @DisplayName("Ability exiles Mother Bear and creates two 2/2 green Bear tokens")
    void abilityExilesSourceAndCreatesBearTokens() {
        setUpAbility();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Mother Bear");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mother Bear"));

        harness.passBothPriorities();

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bear"))
                .toList();

        assertThat(bears).hasSize(2);
        assertThat(bears).allSatisfy(bear -> {
            assertThat(bear.getCard().getPower()).isEqualTo(2);
            assertThat(bear.getCard().getToughness()).isEqualTo(2);
            assertThat(bear.getCard().getColors()).containsExactly(CardColor.GREEN);
            assertThat(bear.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
        });
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new MotherBear()));
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Mother Bear");
    }

    @Test
    @DisplayName("Ability cannot be activated outside a main phase")
    void cannotActivateDuringCombat() {
        setUpAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Mother Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability cannot be activated with a spell on the stack")
    void cannotActivateWithNonemptyStack() {
        setUpAbility();
        harness.castFromHand(player1, new MotherBear(), "{1}{G}");

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Mother Bear");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability can be activated during the postcombat main phase")
    void canActivateDuringPostcombatMain() {
        setUpAbility();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Mother Bear");
    }

    @Test
    @DisplayName("Insufficient green mana prevents activation without exiling the source")
    void cannotActivateWithoutTwoGreenMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Mother Bear");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling the source is paid before tokens are created")
    void exileIsPaidBeforeResolution() {
        setUpAbility();
        MotherBear source = (MotherBear) gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
    }

    private void setUpAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MotherBear()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
