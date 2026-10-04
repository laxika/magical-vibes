package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.n.NikaraLairScavenger;
import com.github.laxika.magicalvibes.cards.t.TheFantasticar;
import com.github.laxika.magicalvibes.cards.y.YannikScavengingSentinel;
import com.github.laxika.magicalvibes.cards.z.ZuriWarriorOfWakanda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessRanksOfHYDRA.class, ZuriWarriorOfWakanda.class, TheFantasticar.class,
        NikaraLairScavenger.class, YannikScavengingSentinel.class})
class EndlessRanksOfHYDRATest extends BaseCardTest {

    @Test
    @DisplayName("Creates one menace Villain for each opponent")
    void createsVillainForEachOpponent() {
        harness.castFromHand(player1, new EndlessRanksOfHYDRA(), "{3}{B}");
        resolveAllTriggers();

        List<Permanent> villains = findPermanents(player1, "Villain").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(villains).hasSize(1);
        assertThat(villains.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(villains.getFirst().getEffectiveToughness()).isEqualTo(1);
        assertThat(villains.getFirst().hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A commander entering lets you pay to return the card from the graveyard")
    void commanderEnteringReturnsCard() {
        EndlessRanksOfHYDRA ranks = new EndlessRanksOfHYDRA();
        harness.setGraveyard(player1, List.of(ranks));

        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        harness.castFromHand(player1, commander, "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(ranks.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(ranks.getId()));
    }

    @Test
    @DisplayName("A commander attack lets you pay to return the card from the graveyard")
    void commanderAttackingReturnsCard() {
        EndlessRanksOfHYDRA ranks = new EndlessRanksOfHYDRA();
        harness.setGraveyard(player1, List.of(ranks));

        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(ranks.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(ranks.getId()));
    }

    @Test
    @DisplayName("A noncommander attack does not trigger the graveyard ability")
    void noncommanderAttackDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new EndlessRanksOfHYDRA()));
        addCreatureReady(player1, new ZuriWarriorOfWakanda());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("A noncreature commander entering also triggers the graveyard ability")
    void noncreatureCommanderEnteringTriggers() {
        EndlessRanksOfHYDRA ranks = new EndlessRanksOfHYDRA();
        harness.setGraveyard(player1, List.of(ranks));

        Card commander = new TheFantasticar();
        gd.makeCommander(player1.getId(), commander);
        harness.enterBattlefieldAndReturn(player1, commander);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("The attack trigger resolves after the commander leaves the battlefield")
    void attackTriggerSurvivesCommanderLeaving() {
        EndlessRanksOfHYDRA ranks = new EndlessRanksOfHYDRA();
        harness.setGraveyard(player1, List.of(ranks));
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        Permanent permanent = addCreatureReady(player1, commander);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        harness.setExile(player1, List.of(commander));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(ranks);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ranks);
    }

    @Test
    @DisplayName("Your commander attacking under another player's control still triggers")
    void ownCommanderAttackingUnderOpponentControlTriggers() {
        harness.setGraveyard(player1, List.of(new EndlessRanksOfHYDRA()));
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player2, commander);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("An opponent's commander attacking under your control does not trigger")
    void opposingCommanderAttackingUnderOwnControlDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new EndlessRanksOfHYDRA()));
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player2.getId(), commander);
        addCreatureReady(player1, commander);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Your commander entering under another player's control still triggers")
    void ownCommanderEnteringUnderOpponentControlTriggers() {
        harness.setGraveyard(player1, List.of(new EndlessRanksOfHYDRA()));
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);

        harness.enterBattlefieldAndReturn(player2, commander);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("An opponent's commander entering under your control does not trigger")
    void opposingCommanderEnteringUnderOwnControlDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new EndlessRanksOfHYDRA()));
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player2.getId(), commander);

        harness.enterBattlefieldAndReturn(player1, commander);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Declining the payment leaves the card in your graveyard")
    void decliningPaymentLeavesCardInGraveyard() {
        EndlessRanksOfHYDRA ranks = new EndlessRanksOfHYDRA();
        harness.setGraveyard(player1, List.of(ranks));
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.enterBattlefieldAndReturn(player1, commander);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ranks);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ranks);
    }

    @Test
    @DisplayName("A noncommander entering does not trigger the graveyard ability")
    void noncommanderEnteringDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new EndlessRanksOfHYDRA()));

        harness.enterBattlefieldAndReturn(player1, new ZuriWarriorOfWakanda());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("The return ability does not trigger while the card is in hand")
    void cardInHandDoesNotTrigger() {
        harness.setHand(player1, List.of(new EndlessRanksOfHYDRA()));
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        harness.enterBattlefieldAndReturn(player1, commander);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @CardUsed({EndlessRanksOfHYDRA.class, NikaraLairScavenger.class, YannikScavengingSentinel.class})
    @DisplayName("Both partner commanders attacking create separate return triggers")
    void bothPartnerCommandersAttackingTriggerSeparately() {
        harness.setGraveyard(player1, List.of(new EndlessRanksOfHYDRA()));
        Card nikara = new NikaraLairScavenger();
        Card yannik = new YannikScavengingSentinel();
        gd.playerCommanders.put(player1.getId(), new ArrayList<>(List.of(nikara, yannik)));
        addCreatureReady(player1, nikara);
        addCreatureReady(player1, yannik);

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
    }
}
