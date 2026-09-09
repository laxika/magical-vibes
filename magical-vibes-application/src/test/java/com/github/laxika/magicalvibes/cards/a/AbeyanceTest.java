package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.NaturesResurgence;
import com.github.laxika.magicalvibes.cards.v.Vitalize;
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

@CardUsed({Abeyance.class, BenalishInfantry.class, MindStone.class,
        NaturesResurgence.class, Vitalize.class})
class AbeyanceTest extends BaseCardTest {

    /** Player1 casts Abeyance at player2 on player1's postcombat main phase. */
    private void castAbeyanceAtPlayer2() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Abeyance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target player can't cast an instant")
    void targetCantCastInstant() {
        castAbeyanceAtPlayer2();

        Vitalize instant = new Vitalize();

        assertThatThrownBy(() -> harness.castFromHand(player2, instant, "{G}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant);
    }

    @Test
    @DisplayName("Target player can't cast a sorcery")
    void targetCantCastSorcery() {
        castAbeyanceAtPlayer2();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        NaturesResurgence sorcery = new NaturesResurgence();

        assertThatThrownBy(() -> harness.castFromHand(player2, sorcery, "{2}{G}{G}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(sorcery);
    }

    @Test
    @DisplayName("Abeyance can target only a player")
    void targetMustBePlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());
        Abeyance spell = new Abeyance();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Target player can still cast a creature spell")
    void targetCanStillCastCreature() {
        castAbeyanceAtPlayer2();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new BenalishInfantry(), "{2}{W}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A player other than the target can still cast an instant")
    void nonTargetPlayerCanStillCastInstant() {
        castAbeyanceAtPlayer2();

        Vitalize instant = new Vitalize();
        harness.castFromHand(player1, instant, "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
    }

    @Test
    @DisplayName("Target player can't activate a non-mana ability")
    void targetCantActivateNonManaAbility() {
        castAbeyanceAtPlayer2();

        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("aren't mana abilities");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(mindStone);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Target player can still activate mana abilities")
    void targetCanStillActivateManaAbility() {
        castAbeyanceAtPlayer2();

        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(mindStone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller draws a card")
    void controllerDrawsACard() {
        BenalishInfantry drawnCard = new BenalishInfantry();
        harness.setLibrary(player1, List.of(drawnCard));
        castAbeyanceAtPlayer2();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Restrictions wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        castAbeyanceAtPlayer2();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Vitalize(), "{G}");
        harness.passBothPriorities();

        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mindStone);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(mindStone.getCard());
    }
}
