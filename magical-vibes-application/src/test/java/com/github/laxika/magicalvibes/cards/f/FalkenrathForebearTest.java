package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FalkenrathForebear.class, GrizzlyBears.class})
class FalkenrathForebearTest extends BaseCardTest {

    @Test
    @DisplayName("Falkenrath Forebear can't block")
    void cantBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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

    private void addBloodToken(com.github.laxika.magicalvibes.model.Player player) {
        Card bloodCard = new Card();
        bloodCard.setName("Blood");
        bloodCard.setType(CardType.ARTIFACT);
        bloodCard.setManaCost("");
        bloodCard.setToken(true);
        bloodCard.setSubtypes(List.of(CardSubtype.BLOOD));

        Permanent blood = new Permanent(bloodCard);
        blood.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blood);
    }
}
