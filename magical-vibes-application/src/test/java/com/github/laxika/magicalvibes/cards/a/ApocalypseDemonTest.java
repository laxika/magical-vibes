package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApocalypseDemonTest extends BaseCardTest {

    // ===== Dynamic power/toughness =====

    @Test
    @DisplayName("P/T equals the number of cards in the controller's graveyard, of any type")
    void ptEqualsCardsInOwnGraveyard() {
        Permanent perm = addDemonReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GrizzlyBears());
        graveyard.add(new Plains());
        graveyard.add(new MindRot());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    @Test
    @DisplayName("P/T counts only the controller's graveyard, not the opponent's")
    void ptCountsOnlyControllerGraveyard() {
        Permanent perm = addDemonReady(player1);
        harness.setGraveyard(player1, createCards(3));
        harness.setGraveyard(player2, createCards(5));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    // ===== Upkeep tap-unless-sacrifice trigger =====

    @Test
    @DisplayName("Declining the sacrifice taps the Demon")
    void decliningTapsDemon() {
        harness.setGraveyard(player1, createCards(3)); // Demon is 3/3, survives SBA
        harness.addToBattlefield(player1, new ApocalypseDemon());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(demon(player1).isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sacrificing another creature leaves the Demon untapped")
    void sacrificingLeavesDemonUntapped() {
        harness.setGraveyard(player1, createCards(3));
        harness.addToBattlefield(player1, new ApocalypseDemon());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(demon(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("With no other creature to sacrifice, the Demon is tapped without a prompt")
    void noOtherCreatureTapsDemon() {
        harness.setGraveyard(player1, createCards(3));
        harness.addToBattlefield(player1, new ApocalypseDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → can't sacrifice → tap

        assertThat(demon(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.setGraveyard(player1, createCards(3));
        harness.addToBattlefield(player1, new ApocalypseDemon());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(demon(player1).isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    private Permanent demon(Player owner) {
        UUID id = harness.getPermanentId(owner, "Apocalypse Demon");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    private Permanent addDemonReady(Player player) {
        Permanent perm = new Permanent(new ApocalypseDemon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> createCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
