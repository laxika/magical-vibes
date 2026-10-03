package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdornedPouncer.class})
class AdornedPouncerTest extends BaseCardTest {

    private void setUpEternalize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new AdornedPouncer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent eternalizedToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Adorned Pouncer") && p.getCard().isToken())
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Eternalize exiles the source card from the graveyard as a cost")
    void eternalizeExilesSourceAsCost() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Adorned Pouncer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Adorned Pouncer"));
    }

    @Test
    @DisplayName("Eternalize creates a 4/4 black Zombie Cat token copy with no mana cost")
    void eternalizeCreatesFourFourBlackZombieCatToken() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Eternalize ability

        Permanent token = eternalizedToken();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getColors()).contains(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.CAT);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Eternalize token keeps Double strike as a copied keyword")
    void eternalizeTokenKeepsDoubleStrike() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, eternalizedToken(), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Eternalize can only be activated at sorcery speed")
    void eternalizeOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new AdornedPouncer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Adorned Pouncer");
    }

    @Test
    void eternalizeCannotBeActivatedDuringCombat() {
        setUpEternalize();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Adorned Pouncer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eternalizeCannotBeActivatedWithAnAbilityOnTheStack() {
        setUpEternalize();
        harness.setGraveyard(player1, List.of(new AdornedPouncer(), new AdornedPouncer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 0);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Adorned Pouncer");
    }

    @Test
    void originalDealsDoubleStrikeCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AdornedPouncer());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 18);
    }

    @Test
    void eternalizedTokenDealsDoubleStrikeCombatDamage() {
        setUpEternalize();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent attacker = eternalizedToken();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 12);
    }
}
