package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SteadfastSentinel.class})
class SteadfastSentinelTest extends BaseCardTest {

    private void setUpEternalize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SteadfastSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent eternalizedToken() {
        return findPermanents(player1, "Steadfast Sentinel").stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Eternalize exiles the source card from the graveyard as a cost")
    void eternalizeExilesSourceAsCost() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Steadfast Sentinel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Steadfast Sentinel"));
    }

    @Test
    @DisplayName("Eternalize creates a 4/4 black Zombie Human Cleric token copy with no mana cost")
    void eternalizeCreatesFourFourBlackZombieToken() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Eternalize ability

        Permanent token = eternalizedToken();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.HUMAN, CardSubtype.CLERIC);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Eternalize token keeps Vigilance as a copied keyword")
    void eternalizeTokenKeepsVigilance() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, eternalizedToken(), Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Eternalize can only be activated at sorcery speed")
    void eternalizeOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new SteadfastSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Opponent's turn Ă˘â‚¬â€ť not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Steadfast Sentinel");
    }

    @Test
    @DisplayName("Eternalize cannot be activated during combat on your own turn")
    void eternalizeCannotBeActivatedDuringCombat() {
        setUpEternalize();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Steadfast Sentinel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Eternalize cannot be activated while another ability is on the stack")
    void eternalizeRequiresEmptyStack() {
        setUpEternalize();
        harness.setGraveyard(player1, List.of(new SteadfastSentinel(), new SteadfastSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Eternalize requires two white mana even when enough total mana is available")
    void eternalizeRequiresTwoWhiteMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SteadfastSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Steadfast Sentinel");
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Steadfast Sentinel");
    }

    @Test
    @DisplayName("Eternalize can be activated during the postcombat main phase")
    void eternalizeWorksDuringPostcombatMain() {
        setUpEternalize();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.assertNotOnBattlefield(player1, "Steadfast Sentinel");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Steadfast Sentinel");
        assertThat(eternalizedToken().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance keeps the original creature untapped when attacking")
    void originalCreatureAttacksWithoutTapping() {
        Permanent sentinel = addCreatureReady(player1, new SteadfastSentinel());

        declareAttackers(List.of(0));

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The eternalized token attacks without tapping")
    void eternalizedTokenAttacksWithoutTapping() {
        setUpEternalize();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent token = eternalizedToken();
        token.setSummoningSick(false);

        declareAttackers(List.of(0));

        assertThat(token.isTapped()).isFalse();
    }
}
