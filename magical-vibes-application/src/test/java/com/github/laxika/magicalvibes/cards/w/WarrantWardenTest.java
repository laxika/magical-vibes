package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({WarrantWarden.class, SauroformHybrid.class, Plains.class})
class WarrantWardenTest extends BaseCardTest {

    private static final int WARRANT = 0;
    private static final int WARDEN = 1;

    @Test
    @DisplayName("Warrant puts an attacking creature on top of its owner's library")
    void warrantPutsAttackingCreatureOnTopOfLibrary() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        attacker.setAttacking(true);
        harness.setLibrary(player2, List.of(new Plains()));

        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castModalInstant(player1, 0, WARRANT, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(attacker.getCard());
    }

    @Test
    @DisplayName("Warrant can target a blocking creature")
    void warrantPutsBlockingCreatureOnTopOfLibrary() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        blocker.setBlocking(true);
        harness.setLibrary(player2, List.of(new Plains()));

        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, WARRANT, List.of(blocker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(blocker.getCard());
    }

    @Test
    @DisplayName("Warrant cannot target a creature that is not attacking or blocking")
    void warrantCannotTargetNonCombatCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, WARRANT, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Warden creates a 4/4 white and blue Sphinx with flying and vigilance")
    void wardenCreatesSphinxToken() {
        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, WARDEN, List.of());
        harness.passBothPriorities();

        Permanent sphinx = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(sphinx.getCard().getName()).isEqualTo("Sphinx");
        assertThat(sphinx.getCard().getPower()).isEqualTo(4);
        assertThat(sphinx.getCard().getToughness()).isEqualTo(4);
        assertThat(sphinx.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(sphinx.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(sphinx.getCard().getSubtypes()).containsExactly(CardSubtype.SPHINX);
        assertThat(sphinx.getCard().getKeywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Warden cannot be cast during combat")
    void wardenCannotBeCastDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, WARDEN, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Warden cannot be cast during an opponent's turn")
    void wardenCannotBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, WARDEN, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Warden cannot be cast in response to a spell")
    void wardenCannotBeCastWithNonemptyStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new WarrantWarden(), new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstant(player1, 0, WARRANT, List.of(attacker.getId()));

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, WARDEN, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Warrant can be cast during opposing combat with mixed hybrid mana")
    void warrantCanBeCastDuringOpposingCombatWithMixedMana() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        attacker.setAttacking(true);
        harness.setLibrary(player2, List.of(new Plains()));
        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, WARRANT, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(attacker.getCard());
    }

    @Test
    @DisplayName("Warrant does not move a creature that is no longer attacking or blocking")
    void warrantRechecksCombatStatusOnResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        attacker.setAttacking(true);
        Plains originalTop = new Plains();
        harness.setLibrary(player2, List.of(originalTop));
        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstant(player1, 0, WARRANT, List.of(attacker.getId()));
        attacker.setAttacking(false);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sauroform Hybrid");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(originalTop);
        harness.assertInGraveyard(player1, "Warrant // Warden");
    }

    @Test
    @DisplayName("Warrant returns a stolen creature to its owner's library")
    void warrantUsesOwnerLibraryRatherThanControllerLibrary() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        gd.stolenCreatures.put(attacker.getId(), player1.getId());
        attacker.setAttacking(true);
        Plains ownerTop = new Plains();
        Plains controllerTop = new Plains();
        harness.setLibrary(player1, List.of(ownerTop));
        harness.setLibrary(player2, List.of(controllerTop));
        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castModalInstant(player1, 0, WARRANT, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(attacker.getCard(), ownerTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerTop);
    }
}
