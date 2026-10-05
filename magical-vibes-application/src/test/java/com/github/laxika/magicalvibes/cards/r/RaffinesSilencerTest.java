package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SnoopingNewsie;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaffinesSilencer.class, SnoopingNewsie.class, Mountain.class, Murder.class, RayOfCommand.class})
class RaffinesSilencerTest extends BaseCardTest {

    @Test
    void enteringConnivesAndAddsCounterForNonlandDiscard() {
        harness.setHand(player1, List.of(new RaffinesSilencer(), new Mountain()));
        harness.setLibrary(player1, List.of(new SnoopingNewsie()));
        addSilencerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent silencer = findPermanent(player1, "Raffine's Silencer");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Snooping Newsie");

        assertThat(silencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringConnivesWithoutCounterForLandDiscard() {
        harness.setHand(player1, List.of(new RaffinesSilencer(), new SnoopingNewsie()));
        harness.setLibrary(player1, List.of(new Mountain()));
        addSilencerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent silencer = findPermanent(player1, "Raffine's Silencer");
        discardByName("Mountain");

        assertThat(silencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathTriggerUsesSilencersPowerAndTargetsOnlyOpponentCreatures() {
        harness.setHand(player1, List.of(new RaffinesSilencer(), new Murder(), new Mountain()));
        harness.setLibrary(player1, List.of(new SnoopingNewsie()));
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new SnoopingNewsie());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new SnoopingNewsie());
        addSilencerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        discardByName("Snooping Newsie");

        Permanent silencer = findPermanent(player1, "Raffine's Silencer");
        UUID silencerId = silencer.getId();
        UUID opponentBearsId = opponentBears.getId();
        UUID ownBearsId = ownBears.getId();

        harness.castAndResolveInstant(player1, 0, silencerId);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(opponentBearsId);
        assertThat(choice.validIds()).doesNotContain(ownBearsId);

        harness.handlePermanentChosen(player1, opponentBearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentBearsId));
        harness.assertInGraveyard(player2, "Snooping Newsie");
    }

    @Test
    void connivesEvenIfSilencerDiesBeforeItsEnterTriggerResolves() {
        harness.setHand(player1, List.of(new RaffinesSilencer(), new Murder(), new Mountain()));
        harness.setLibrary(player1, List.of(new SnoopingNewsie()));
        addSilencerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent silencer = findPermanent(player1, "Raffine's Silencer");
        harness.castAndResolveInstant(player1, 0, silencer.getId());
        harness.assertInGraveyard(player1, "Raffine's Silencer");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Snooping Newsie");

        harness.assertInGraveyard(player1, "Snooping Newsie");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void deathTriggerWithoutConniveCounterExpiresAtEndOfTurn() {
        Permanent silencer = harness.addToBattlefieldAndReturn(player1, new RaffinesSilencer());
        Permanent newsie = harness.addToBattlefieldAndReturn(player2, new SnoopingNewsie());
        harness.setHand(player1, List.of(new Murder()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, silencer.getId());
        harness.handlePermanentChosen(player1, newsie.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, newsie)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, newsie)).isEqualTo(1);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, newsie)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, newsie)).isEqualTo(2);
    }

    @Test
    void deathTriggerDoesNotAffectAnotherCreatureIfItsTargetLeaves() {
        Permanent silencer = harness.addToBattlefieldAndReturn(player1, new RaffinesSilencer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnoopingNewsie());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SnoopingNewsie());
        harness.setHand(player1, List.of(new Murder(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, silencer.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Snooping Newsie");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(other);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @CardUsed({RayOfCommand.class})
    void conniveUsesCreaturesCurrentControllerAfterControlChanges() {
        harness.setHand(player1, List.of(new RaffinesSilencer(), new Mountain()));
        harness.setHand(player2, List.of(new RayOfCommand(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new SnoopingNewsie()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent silencer = findPermanent(player1, "Raffine's Silencer");
        harness.castAndResolveInstant(player2, 0, silencer.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(silencer);
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mountain", "Snooping Newsie");
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Snooping Newsie");
        assertThat(silencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addSilencerMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
