package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolsimirMidnightsLight.class, YoungWolf.class, GrizzlyBears.class})
class TolsimirMidnightsLightTest extends BaseCardTest {

    @Test
    void enteringCreatesVojaFenstalker() {
        castTolsimir();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent voja = findPermanent(player1, "Voja Fenstalker");
        assertThat(voja.getCard().isToken()).isTrue();
        assertThat(voja.getCard().getPower()).isEqualTo(5);
        assertThat(voja.getCard().getToughness()).isEqualTo(5);
        assertThat(voja.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(voja.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(voja.getCard().getSupertypes()).containsExactly(CardSupertype.LEGENDARY);
        assertThat(voja.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    void wolfAttackAfterTolsimirAttacksMakesChosenOpponentCreatureBlockIt() {
        Permanent tolsimir = readyCreature(player1, new TolsimirMidnightsLight());
        Permanent wolf = readyCreature(player1, new YoungWolf());
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(blocker.getId());

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).containsExactly(wolf.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void wolfAttackDoesNotTriggerUnlessTolsimirAttacked() {
        readyCreature(player1, new TolsimirMidnightsLight());
        Permanent wolf = readyCreature(player1, new YoungWolf());
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(blocker.getMustBlockIds()).isEmpty();
        assertThat(wolf.isAttacking()).isTrue();
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castTolsimir() {
        harness.setHand(player1, List.of(new TolsimirMidnightsLight()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
