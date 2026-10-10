package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.cards.o.OltecCloudGuard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DigsiteConservator.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class,
        AirElemental.class, LightningBolt.class, QuintoriusKand.class, OltecCloudGuard.class})
class DigsiteConservatorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability exiles up to four cards from a single graveyard")
    void exilesUpToFourCardsFromSingleGraveyard() {
        Permanent conservator = addReadyConservator(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        Card card3 = new Forest();
        Card card4 = new AirElemental();
        harness.setGraveyard(player2, List.of(card1, card2, card3, card4));

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(conservator), 0,
                List.of(card1.getId(), card2.getId(), card3.getId(), card4.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(conservator);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(card1.getId(), card2.getId(), card3.getId(), card4.getId());
    }

    @Test
    @DisplayName("Sacrifice ability requires all targets to come from one graveyard")
    void targetsMustShareOneGraveyard() {
        Permanent conservator = addReadyConservator(player1);
        Card mine = new GrizzlyBears();
        Card theirs = new LightningBolt();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(conservator), 0, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated at sorcery speed")
    void abilityRequiresSorcerySpeed() {
        Permanent conservator = addReadyConservator(player1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(conservator), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Death trigger may pay four to discover four")
    void deathTriggerDiscoversFourWhenPaid() {
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new DigsiteConservator());
        CounselOfTheSoratami discovered = new CounselOfTheSoratami();
        Forest land = new Forest();
        AirElemental expensive = new AirElemental();
        GrizzlyBears below = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, expensive, discovered, below));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, conservator));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, expensive, below);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Sacrificing with no targets still triggers discover")
    void canSacrificeWithZeroTargets() {
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new DigsiteConservator());
        DigsiteConservator discovered = new DigsiteConservator();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(discovered));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(conservator), 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(conservator);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(conservator.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the death payment leaves the library and mana unchanged")
    void canDeclineDeathPayment() {
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new DigsiteConservator());
        Card top = new DigsiteConservator();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, conservator));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    @DisplayName("Remaining legal graveyard targets are exiled when another target leaves")
    void exilesRemainingLegalTargets() {
        Permanent conservator = addReadyConservator(player1);
        Card departed = new DigsiteConservator();
        Card remaining = new Forest();
        harness.setGraveyard(player2, List.of(departed, remaining));
        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(conservator), 0,
                List.of(departed.getId(), remaining.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(departed));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).contains(departed);
    }

    @Test
    @DisplayName("Discover puts the revealed cards in exile while the choice is pending")
    void discoveredCardsAreInExileBeforeChoice() {
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new DigsiteConservator());
        Forest skipped = new Forest();
        DigsiteConservator discovered = new DigsiteConservator();
        harness.setLibrary(player1, List.of(skipped, discovered));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, conservator));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(skipped, discovered);
    }

    @Test
    @DisplayName("Casting the discovered card triggers Quintorius Kand")
    void discoveredSpellIsCastFromExile() {
        harness.addToBattlefieldAndReturn(player1, new QuintoriusKand())
                .setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 4);
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new DigsiteConservator());
        DigsiteConservator discovered = new DigsiteConservator();
        harness.setLibrary(player1, List.of(discovered));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int myLife = gd.getLife(player1.getId());
        int theirLife = gd.getLife(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, conservator));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(myLife + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(theirLife - 2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == discovered);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Discover four accepts a card with mana value exactly four")
    void discoversManaValueFour() {
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new DigsiteConservator());
        OltecCloudGuard discovered = new OltecCloudGuard();
        Forest below = new Forest();
        harness.setLibrary(player1, List.of(discovered, below));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, conservator));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Discover with no qualifying card returns all skipped cards to the library")
    void noQualifyingCardReturnsSkippedCards() {
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new DigsiteConservator());
        Forest land = new Forest();
        AirElemental expensive = new AirElemental();
        harness.setLibrary(player1, List.of(land, expensive));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, conservator));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land, expensive);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Permanent addReadyConservator(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DigsiteConservator());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
