package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.t.TransguildCourier;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenthicAnomaly.class, GrizzlyBears.class, HillGiant.class, BurnishedHart.class,
        Cancel.class, CribSwap.class, Mortivore.class, SolemnSimulacrum.class, TransguildCourier.class})
class BenthicAnomalyTest extends BaseCardTest {

    private Player player3;

    @Test
    void createsAColorlessEldraziCopyWithTheSelectedCreaturesTotalPowerAndToughness() {
        addThirdPlayer();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player3, new HillGiant());
        int totalPower = gqs.getEffectivePower(gd, bears) + gqs.getEffectivePower(gd, giant);
        int totalToughness = gqs.getEffectiveToughness(gd, bears) + gqs.getEffectiveToughness(gd, giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BenthicAnomaly(), "{6}{U}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).isEmpty();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELDRAZI);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(totalPower);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(totalToughness);
    }

    @Test
    void createsNoTokenWhenOpponentsControlNoCreatures() {
        harness.addToBattlefield(player1, new BurnishedHart());

        castAnomaly();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Benthic Anomaly");
    }

    @Test
    void enteringWithoutBeingCastDoesNotCreateAToken() {
        harness.addToBattlefield(player2, new BurnishedHart());

        harness.enterBattlefieldAndReturn(player1, new BenthicAnomaly());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void casterChoosesOneCreatureFromEachOpponentBeforeChoosingTheCopy() {
        addThirdPlayer();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player3, new HillGiant());
        Permanent otherGiant = harness.addToBattlefieldAndReturn(player3, new HillGiant());
        harness.addToBattlefield(player1, new BurnishedHart());

        castAnomaly();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice first =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(first.playerId()).isEqualTo(player1.getId());
        assertThat(first.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), otherBears.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        PendingInteraction.PermanentChoice second =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(second.playerId()).isEqualTo(player1.getId());
        assertThat(second.validPermanentIds()).containsExactlyInAnyOrder(giant.getId(), otherGiant.getId());
        harness.handlePermanentChosen(player1, giant.getId());

        PendingInteraction.PermanentChoice copy =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(copy.playerId()).isEqualTo(player1.getId());
        assertThat(copy.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());
        harness.handlePermanentChosen(player1, giant.getId());

        Permanent token = token();
        assertThat(token.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
    }

    @Test
    void skipsAnEmptyOpponentAndUsesCurrentStatsWithoutCopyingCountersOrTappedStatus() {
        addThirdPlayer();
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        hart.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        hart.setPowerModifier(1);
        hart.setToughnessModifier(3);
        hart.tap();

        castAnomaly();
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(7);
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.getEffectiveCardTypes(gd, token)).containsExactly(CardType.CREATURE);
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.ELDRAZI)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.ELK)).isFalse();
    }

    @Test
    void copiedEntersAbilityTriggersBeforeTheAnomalyResolves() {
        harness.addToBattlefield(player2, new SolemnSimulacrum());

        castAnomaly();
        harness.passBothPriorities();

        assertThat(token().getCard().getName()).isEqualTo("Solemn Simulacrum");
        harness.assertNotOnBattlefield(player1, "Benthic Anomaly");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Benthic Anomaly");
    }

    @Test
    void powerAndToughnessRemainFixedWhenTheCopiedCreatureHasDefiningAbilities() {
        harness.setGraveyard(player1, List.of(new BurnishedHart(), new BurnishedHart()));
        Permanent mortivore = harness.addToBattlefieldAndReturn(player2, new Mortivore());

        castAnomaly();
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        harness.setGraveyard(player1, List.of(new BurnishedHart(), new BurnishedHart(), new BurnishedHart()));
        assertThat(gqs.getEffectivePower(gd, mortivore)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void copyingAChangelingTokenDoesNotCopyItsSubtypeDefiningAbility() {
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, hart.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent changeling = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.hasEffectiveSubtype(gd, changeling, CardSubtype.BEAR)).isTrue();

        castAnomaly();
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.ELDRAZI)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.BEAR)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.SHAPESHIFTER)).isFalse();
    }

    @Test
    void copyingAColorDefiningCreatureCreatesAColorlessToken() {
        Permanent courier = harness.addToBattlefieldAndReturn(player2, new TransguildCourier());
        assertThat(gqs.getEffectiveColors(gd, courier)).hasSize(5);

        castAnomaly();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, token())).isEmpty();
        assertThat(gqs.getEffectiveCardTypes(gd, token())).containsExactly(CardType.CREATURE);
    }

    @Test
    void copyingAFaceDownCreatureDoesNotCopyTheHiddenCardsManaCostOrAbilities() {
        Permanent manifested = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        castAnomaly();
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(token.isFaceDown()).isFalse();
        assertThat(token.getCard().getManaCost()).isNullOrEmpty();
        assertThat(token.getCard().getName()).isNullOrEmpty();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.ELDRAZI)).isTrue();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Benthic Anomaly");
    }

    @Test
    void castAbilityStillCreatesATokenWhenTheAnomalySpellIsCountered() {
        harness.addToBattlefield(player2, new BurnishedHart());
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castAnomaly();
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spellId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Benthic Anomaly");

        harness.passBothPriorities();

        assertThat(token().getCard().getName()).isEqualTo("Burnished Hart");
        harness.assertNotOnBattlefield(player1, "Benthic Anomaly");
    }

    private void castAnomaly() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BenthicAnomaly(), "{6}{U}");
    }

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
}
