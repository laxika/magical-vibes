package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdaptiveGemguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RampagingSpiketail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenVolcano.class, Forest.class, GrizzlyBears.class,
        AdaptiveGemguard.class, RampagingSpiketail.class})
class HiddenVolcanoTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new HiddenVolcano()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void tapsForRedMana() {
        Permanent volcano = addReadyVolcano();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(volcano.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndDiscoversFour() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), discovered));
        Permanent volcano = addReadyVolcano();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(volcano.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void castsManaValueFourCardWithoutPayingAndReturnsSkippedCardsToBottom() {
        Forest skippedLand = new Forest();
        RampagingSpiketail skippedCreature = new RampagingSpiketail();
        AdaptiveGemguard discovered = new AdaptiveGemguard();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedCreature, discovered, untouched));
        Permanent volcano = addReadyVolcano();
        payDiscoverCost();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(volcano);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(volcano.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(discovered.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skippedLand, skippedCreature);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Adaptive Gemguard");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
    }

    @Test
    void discoveredSpellIsCastFromExile() {
        AdaptiveGemguard discovered = new AdaptiveGemguard();
        harness.setLibrary(player1, List.of(discovered));
        addReadyVolcano();
        payDiscoverCost();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getCard().getId()).isEqualTo(discovered.getId());
            assertThat(entry.getSourceZone()).isEqualTo(Zone.EXILE);
        });
    }

    @Test
    void noQualifyingCardReturnsEveryCardToLibrary() {
        Forest land = new Forest();
        RampagingSpiketail expensiveCreature = new RampagingSpiketail();
        harness.setLibrary(player1, List.of(land, expensiveCreature));
        addReadyVolcano();
        payDiscoverCost();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, expensiveCreature);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryStillSacrificesVolcano() {
        harness.setLibrary(player1, List.of());
        Permanent volcano = addReadyVolcano();
        payDiscoverCost();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(volcano.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDiscoverOutsideMainPhase() {
        Permanent volcano = addReadyVolcano();
        payDiscoverCost();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(volcano);
        assertThat(volcano.isTapped()).isFalse();
    }

    @Test
    void cannotDiscoverOnOpponentsTurn() {
        Permanent volcano = addReadyVolcano();
        payDiscoverCost();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(volcano);
        assertThat(volcano.isTapped()).isFalse();
    }

    @Test
    void cannotDiscoverWhileAnotherAbilityIsOnStack() {
        harness.setLibrary(player1, List.of());
        addReadyVolcano();
        Permanent secondVolcano = addReadyVolcano();
        payDiscoverCost();
        payDiscoverCost();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondVolcano);
        assertThat(secondVolcano.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    void cannotDiscoverWithoutRedMana() {
        Permanent volcano = addReadyVolcano();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(volcano);
        assertThat(volcano.isTapped()).isFalse();
    }

    @Test
    void tappedVolcanoCannotDiscover() {
        Permanent volcano = addReadyVolcano();
        volcano.tap();
        payDiscoverCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(volcano);
    }

    private void payDiscoverCost() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addReadyVolcano() {
        Permanent volcano = harness.addToBattlefieldAndReturn(player1, new HiddenVolcano());
        volcano.setSummoningSick(false);
        return volcano;
    }
}
