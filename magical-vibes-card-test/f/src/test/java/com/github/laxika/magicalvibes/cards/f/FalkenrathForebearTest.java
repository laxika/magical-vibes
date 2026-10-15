package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SculptingSteel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({FalkenrathForebear.class, SculptingSteel.class})
class FalkenrathForebearTest extends BaseCardTest {

    @Test
    @DisplayName("Falkenrath Forebear can't block")
    void cantBlock() {
        Permanent attacker = addCreatureReady(player1, new FalkenrathForebear());
        addCreatureReady(player2, new FalkenrathForebear());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates a Blood token when dealing combat damage to a player")
    void createsBloodTokenOnCombatDamage() {
        Permanent forebear = addCreatureReady(player1, new FalkenrathForebear());
        forebear.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing two Blood tokens returns Falkenrath Forebear from the graveyard")
    void sacrificesTwoBloodAndReturnsFromGraveyard() {
        FalkenrathForebear forebear = new FalkenrathForebear();
        harness.setGraveyard(player1, List.of(forebear));
        addBloodToken(player1);
        addBloodToken(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forebear);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forebear.getId()));
    }

    @Test
    @DisplayName("Cannot return Falkenrath Forebear without two Blood tokens")
    void requiresTwoBloodTokens() {
        harness.setGraveyard(player1, List.of(new FalkenrathForebear()));
        addBloodToken(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nontoken copy of Blood cannot pay the two Blood tokens cost")
    void cannotSacrificeNontokenBloodCopy() {
        Permanent blood = addBloodToken(player1);
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BLOOD)
                        && !permanent.getCard().isToken());
        harness.setGraveyard(player1, List.of(new FalkenrathForebear()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Falkenrath Forebear");
    }

    @Test
    @DisplayName("An opponent's Blood tokens cannot pay the return cost")
    void cannotSacrificeOpponentsBloodTokens() {
        harness.setGraveyard(player1, List.of(new FalkenrathForebear()));
        addBloodToken(player1);
        addBloodToken(player2);
        addBloodToken(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
        assertThat(countPermanents(player2, "Blood")).isEqualTo(2);
    }

    @Test
    @DisplayName("The return ability requires black mana even with two Blood tokens")
    void cannotReturnWithoutBlackMana() {
        harness.setGraveyard(player1, List.of(new FalkenrathForebear()));
        addBloodToken(player1);
        addBloodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    @DisplayName("The return ability works on an opponent's turn and returns only its source")
    void returnsOnlySourceOnOpponentsTurn() {
        FalkenrathForebear source = new FalkenrathForebear();
        FalkenrathForebear other = new FalkenrathForebear();
        harness.setGraveyard(player1, List.of(source, other));
        addBloodToken(player1);
        addBloodToken(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source, other);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(source.getId());
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.isSummoningSick()).isTrue();
                });
    }

    private Permanent addBloodToken(com.github.laxika.magicalvibes.model.Player player) {
        Card bloodCard = new Card();
        bloodCard.setName("Blood");
        bloodCard.setType(CardType.ARTIFACT);
        bloodCard.setManaCost("");
        bloodCard.setToken(true);
        bloodCard.setSubtypes(List.of(CardSubtype.BLOOD));

        return harness.addToBattlefieldAndReturn(player, bloodCard);
    }
}
