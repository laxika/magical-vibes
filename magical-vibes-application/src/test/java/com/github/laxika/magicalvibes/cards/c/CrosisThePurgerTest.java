package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TowerDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrosisThePurger.class, TowerDrake.class, NomadicElf.class, Forest.class})
class CrosisThePurgerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may be paid for to discard every card of the chosen color")
    void combatDamageDiscardsAllCardsOfChosenColor() {
        Permanent crosis = addCreatureReady(player1, new CrosisThePurger());
        crosis.setAttacking(true);
        harness.setHand(player2, List.of(
                new TowerDrake(), new NomadicElf(), new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Nomadic Elf", "Forest");
        harness.assertInGraveyard(player2, "Tower Drake");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Declining the combat-damage payment does not reveal or discard")
    void decliningPaymentDoesNothing() {
        Permanent crosis = addCreatureReady(player1, new CrosisThePurger());
        crosis.setAttacking(true);
        harness.setHand(player2, List.of(new TowerDrake(), new NomadicElf()));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A paid ability discards every card containing the chosen color")
    void combatDamageDiscardsEveryMatchingCard() {
        Permanent crosis = addCreatureReady(player1, new CrosisThePurger());
        crosis.setAttacking(true);
        harness.setHand(player2, List.of(
                new TowerDrake(), new TowerDrake(), new CrosisThePurger(),
                new NomadicElf(), new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Nomadic Elf", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Tower Drake", "Tower Drake", "Crosis, the Purger");
    }

    @Test
    @DisplayName("No ability triggers when Crosis is blocked and deals no combat damage")
    void blockedCrosisDoesNotTrigger() {
        Permanent crosis = addCreatureReady(player1, new CrosisThePurger());
        crosis.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TowerDrake());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new TowerDrake()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Paying with no cards of the chosen color still reveals the entire hand")
    void noMatchingColorStillRevealsHand() throws Exception {
        Permanent crosis = addCreatureReady(player1, new CrosisThePurger());
        crosis.setAttacking(true);
        harness.setHand(player2, List.of(new NomadicElf(), new Forest()));
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch ->
                events.addAll(batch.events()))) {
            resolveCombat();
            harness.passBothPriorities();
            addPaymentMana();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleListChoice(player1, "BLUE");
        }

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player2.getId());
                    assertThat(reveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::name)
                            .containsExactly("Nomadic Elf", "Forest");
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }

    @Test
    @DisplayName("Crosis may be paid for and a color chosen even when the damaged hand is empty")
    void emptyHandStillAllowsPaymentAndColorChoice() {
        Permanent crosis = addCreatureReady(player1, new CrosisThePurger());
        crosis.setAttacking(true);
        harness.setHand(player2, List.of());

        resolveCombat();
        harness.passBothPriorities();
        addPaymentMana();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addPaymentMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
