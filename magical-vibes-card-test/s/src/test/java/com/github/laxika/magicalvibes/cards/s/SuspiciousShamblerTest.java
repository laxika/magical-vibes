package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SuspiciousShambler.class)
class SuspiciousShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability exiles the source card from the graveyard as a cost")
    void abilityExilesSourceAsCost() {
        setUpAbility();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Suspicious Shambler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Suspicious Shambler"));
    }

    @Test
    @DisplayName("Resolving ability creates two 2/2 black Zombie tokens")
    void resolvingCreatesTwoZombieTokens() {
        setUpAbility();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        List<Permanent> zombies = findPermanents(player1, "Zombie");

        assertThat(zombies).hasSize(2);
        assertThat(zombies).allSatisfy(zombie -> {
            assertThat(zombie.getCard().isToken()).isTrue();
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        });
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new SuspiciousShambler()));
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Suspicious Shambler");
    }

    @Test
    void cannotActivateDuringCombat() {
        setUpAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Suspicious Shambler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithAnotherAbilityOnStack() {
        setUpAbility();
        harness.setGraveyard(player1, List.of(new SuspiciousShambler(), new SuspiciousShambler()));
        addMana();
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Suspicious Shambler");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    void cannotPayWithOnlyOneBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SuspiciousShambler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Suspicious Shambler");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateDuringPostcombatMainPhase() {
        setUpAbility();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void setUpAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SuspiciousShambler()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
