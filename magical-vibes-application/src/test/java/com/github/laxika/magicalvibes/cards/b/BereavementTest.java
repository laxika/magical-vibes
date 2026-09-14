package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.e.Expunge;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PouncingJaguar;
import com.github.laxika.magicalvibes.cards.s.SteamBlast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bereavement.class, CruelEdict.class, Expunge.class, Forest.class, GoblinRaider.class, GrizzlyBears.class, HillGiant.class, PouncingJaguar.class, SteamBlast.class})
class BereavementTest extends BaseCardTest {

    @Test
    @DisplayName("When an opponent's green creature dies, that opponent discards a card")
    void opponentsGreenCreatureDiesOpponentDiscards() {
        harness.addToBattlefield(player1, new Bereavement());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Bereavement trigger → discard choice

        // The DYING creature's controller (player2), not Bereavement's controller, discards.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c instanceof Forest);
    }

    @Test
    @DisplayName("When an opponent's green creature dies, that opponent discards a card")
    void opponentsGreenCreatureDiesOpponentDiscardsUpstreamReview() {
        harness.addToBattlefield(player1, new Bereavement());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new PouncingJaguar());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castExpunge(player1, dyingCreature);

        harness.passBothPriorities(); // resolve Expunge; Pouncing Jaguar dies
        harness.passBothPriorities(); // resolve Bereavement trigger; discard choice

        // The dying creature's controller (player2), not Bereavement's controller, discards.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c instanceof Forest);
    }

    @Test
    @DisplayName("When Bereavement's controller's own green creature dies, that controller discards")
    void ownGreenCreatureDiesControllerDiscards() {
        harness.addToBattlefield(player1, new Bereavement());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Bereavement's controller's own green creature dies, that controller discards")
    void ownGreenCreatureDiesControllerDiscardsUpstreamReview() {
        harness.addToBattlefield(player1, new Bereavement());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castExpunge(player2, dyingCreature);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each green creature dying triggers a separate discard")
    void eachGreenCreatureDiesTriggersSeparately() {
        harness.addToBattlefield(player1, new Bereavement());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict(), new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c instanceof Forest)
                .hasSize(2);
    }

    @Test
    @DisplayName("A non-green creature dying does not trigger Bereavement")
    void nonGreenCreatureDiesNoDiscard() {
        harness.addToBattlefield(player1, new Bereavement());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-green creature dying does not trigger Bereavement")
    void nonGreenCreatureDiesNoDiscardUpstreamReview() {
        harness.addToBattlefield(player1, new Bereavement());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new GoblinRaider());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castExpunge(player1, dyingCreature);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each simultaneously dying green creature causes a separate discard")
    void eachSimultaneouslyDyingGreenCreatureTriggers() {
        harness.addToBattlefield(player1, new Bereavement());
        harness.addToBattlefield(player2, new PouncingJaguar());
        harness.addToBattlefield(player2, new PouncingJaguar());
        harness.setHand(player2, List.of(new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SteamBlast(), "{2}{R}");
        harness.passBothPriorities(); // resolve Steam Blast; both Pouncing Jaguars die
        harness.passBothPriorities(); // resolve one Bereavement trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.passBothPriorities(); // resolve the second Bereavement trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card instanceof Forest)
                .count()).isEqualTo(2);
    }

    private void castExpunge(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Expunge()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, target.getId());
    }
}
