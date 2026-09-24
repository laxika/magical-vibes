package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GalactusDevourerOfWorlds.class, GrizzlyBears.class})
class GalactusDevourerOfWorldsTest extends BaseCardTest {

    @Test
    @DisplayName("When Galactus enters, it exiles target permanent")
    void etbExilesTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGalactus(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Galactus must attack each combat when able")
    void mustAttackEachCombat() {
        Permanent galactus = addCreatureReady(player1, new GalactusDevourerOfWorlds());
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThat(galactus.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Galactus may stay back while its controller controls Silver Surfer")
    void silverSurferRemovesAttackRequirement() {
        Permanent galactus = addCreatureReady(player1, new GalactusDevourerOfWorlds());
        harness.addToBattlefield(player1, silverSurfer());
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(galactus.isAttacking()).isFalse();
    }

    private void castGalactus(Permanent target) {
        harness.setHand(player1, List.of(new GalactusDevourerOfWorlds()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Card silverSurfer() {
        Card card = new Card();
        card.setName("Silver Surfer, Galactus's Herald");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
