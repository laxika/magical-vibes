package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoldarenPariahTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing three other creatures transforms Voldaren Pariah")
    void sacrificesThreeOtherCreaturesAndTransforms() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.getCard().getName()).isEqualTo("Abolisher of Bloodlines");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard(), third.getCard());
    }

    @Test
    @DisplayName("Abolisher of Bloodlines makes the targeted opponent sacrifice three creatures")
    void targetedOpponentSacrificesThreeCreatures() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentFirst = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentSecond = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentThird = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentRemaining = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(opponentFirst.getId(), opponentSecond.getId(), opponentThird.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentRemaining);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opponentFirst.getCard(), opponentSecond.getCard(), opponentThird.getCard());
    }

    @Test
    @DisplayName("The transform trigger cannot target its controller")
    void transformTriggerCannotTargetController() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Madness offers Voldaren Pariah for {B}{B}{B}")
    void madnessOffersCast() {
        VoldarenPariah pariah = discardPariahViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(pariah.getId()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private VoldarenPariah discardPariahViaRavensCrime() {
        VoldarenPariah pariah = new VoldarenPariah();
        harness.setHand(player1, List.of(pariah));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return pariah;
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
