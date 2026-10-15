package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TyvarJubilantBrawler.class, Forest.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class TyvarJubilantBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Static ability lets summoning-sick creatures activate their abilities")
    void summoningSickCreatureCanActivateAbility() {
        harness.addToBattlefield(player1, new TyvarJubilantBrawler());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("+1 untaps up to one target creature")
    void plusOneUntapsTargetCreature() {
        Permanent tyvar = addReadyTyvar(player1, 4);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(tyvar.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 may choose no creature")
    void plusOneMayChooseNoCreature() {
        addReadyTyvar(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-2 mills three cards and may return a creature with mana value 2 or less")
    void minusTwoMillsAndReturnsQualifyingCreature() {
        Permanent tyvar = addReadyTyvar(player1, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Card expensiveCreature = new HillGiant();
        Card qualifyingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(expensiveCreature, qualifyingCreature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(tyvar.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        int qualifyingIndex = choice.validIndices().stream()
                .filter(index -> gd.playerGraveyards.get(player1.getId()).get(index).getId()
                        .equals(qualifyingCreature.getId()))
                .findFirst()
                .orElseThrow();
        harness.handleGraveyardCardChosen(player1, qualifyingIndex);

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expensiveCreature);
    }

    @Test
    @DisplayName("+1 cannot target a noncreature permanent")
    void plusOneCannotTargetLand() {
        addReadyTyvar(player1, 4);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Static ability does not let summoning-sick creatures attack")
    void summoningSickCreatureCannotAttack() {
        harness.addToBattlefield(player1, new TyvarJubilantBrawler());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Static ability does not let opposing summoning-sick creatures activate tap abilities")
    void opponentCannotActivateSummoningSickCreature() {
        harness.addToBattlefield(player1, new TyvarJubilantBrawler());
        harness.addToBattlefield(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("-2 can return a creature just milled and let it activate its tap ability immediately")
    void minusTwoReturnsNewlyMilledCreature() {
        addReadyTyvar(player1, 4);
        Card creature = new LlanowarElves();
        Card remainingCard = new Forest();
        harness.setLibrary(player1, List.of(creature, new Forest(), new Forest(), remainingCard));
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature).hasSize(3);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 still mills when the controller declines to return a creature")
    void minusTwoMayDeclineReturn() {
        addReadyTyvar(player1, 4);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature).hasSize(4);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-2 can return a creature with fewer than three cards left in the library")
    void minusTwoReturnsCreatureAfterMillingShortLibrary() {
        addReadyTyvar(player1, 4);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-2 excludes expensive creatures, noncreatures, and opponents' graveyards")
    void minusTwoOnlyOffersEligibleCardsFromOwnGraveyard() {
        addReadyTyvar(player1, 4);
        Card expensiveCreature = new HillGiant();
        Card eligibleCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(expensiveCreature, eligibleCreature, new Forest()));
        Card opposingCreature = new LlanowarElves();
        harness.setGraveyard(player2, List.of(opposingCreature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        int eligibleIndex = gd.playerGraveyards.get(player1.getId()).indexOf(eligibleCreature);
        assertThat(choice.validIndices()).containsExactly(eligibleIndex);
        harness.handleGraveyardCardChosen(player1, eligibleIndex);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expensiveCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    @DisplayName("-2 resolves even when paying its cost puts Tyvar into the graveyard")
    void minusTwoResolvesAfterTyvarDies() {
        addReadyTyvar(player1, 2);
        Card creature = new LlanowarElves();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Tyvar, Jubilant Brawler");
        harness.assertInGraveyard(player1, "Tyvar, Jubilant Brawler");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTyvar(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TyvarJubilantBrawler());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
