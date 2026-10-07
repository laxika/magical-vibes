package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningStorm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
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

@CardUsed({TitheTaker.class, AdantoVanguard.class, Shock.class, YavimayaCoast.class, LightningStorm.class, Forest.class})
class TitheTakerTest extends BaseCardTest {

    @Test
    @DisplayName("Taxes an opponent's spell during the controller's turn")
    void taxesOpponentsSpellDuringControllersTurn() {
        harness.addToBattlefield(player1, new TitheTaker());
        prepareTurn(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not tax an opponent's spell during that opponent's turn")
    void doesNotTaxOpponentsSpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new TitheTaker());
        prepareTurn(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Taxes an opponent's non-mana ability")
    void taxesOpponentsNonManaAbility() {
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player2, new AdantoVanguard());
        prepareTurn(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not tax an opponent's mana ability")
    void doesNotTaxOpponentsManaAbility() {
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player2, new YavimayaCoast());
        prepareTurn(player1);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Afterlife creates a Spirit token when Tithe Taker dies")
    void afterlifeCreatesSpiritWhenItDies() {
        Permanent titheTaker = harness.addToBattlefieldAndReturn(player1, new TitheTaker());
        prepareTurn(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, titheTaker.getId());

        harness.assertInGraveyard(player1, "Tithe Taker");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not tax its controller's spells during their turn")
    void doesNotTaxControllersSpell() {
        harness.addToBattlefield(player1, new TitheTaker());
        prepareTurn(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not tax its controller's non-mana abilities")
    void doesNotTaxControllersAbility() {
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player1, new AdantoVanguard());
        prepareTurn(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Does not tax an opponent's non-mana abilities on their turn")
    void doesNotTaxOpponentsAbilityOnOpponentsTurn() {
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player2, new AdantoVanguard());
        prepareTurn(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Two Tithe Takers each tax an opponent's spell")
    void spellTaxesAreCumulative() {
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        prepareTurn(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Two Tithe Takers each tax an opponent's non-mana ability")
    void abilityTaxesAreCumulative() {
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player2, new AdantoVanguard());
        prepareTurn(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Afterlife creates an untapped white and black flying Spirit for its controller")
    void afterlifeCreatesCorrectTokenOnOpponentsTurn() {
        Permanent titheTaker = harness.addToBattlefieldAndReturn(player1, new TitheTaker());
        prepareTurn(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, titheTaker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tithe Taker");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player2, "Spirit")).isZero();
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.isTapped()).isFalse();
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @CardUsed({LightningStorm.class, Forest.class})
    @DisplayName("Taxes an opponent's activation of an ability on a spell on the stack")
    void taxesOpponentsStackAbility() {
        harness.addToBattlefield(player1, new TitheTaker());
        prepareTurn(player1);
        LightningStorm storm = new LightningStorm();
        harness.setHand(player1, List.of(storm));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateStackAbility(player2, storm.getId(), 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateStackAbility(player2, storm.getId(), 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void prepareTurn(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
