package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectorsCage.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class CollectorsCageTest extends BaseCardTest {

    private Permanent addCageWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new CollectorsCage());
        Permanent cage = findPermanent(player1, "Collector's Cage");
        gd.setImprintedCard(cage.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return cage;
    }

    @Test
    @DisplayName("Hideaway 5 exiles the chosen card face down and randomly bottoms the rest")
    void hideawayEtbExilesChosenCard() {
        Card chosen = new GrizzlyBears();
        Card bottomOne = new AirElemental();
        Card bottomTwo = new HillGiant();
        Card bottomThree = new LlanowarElves();
        Card bottomFour = new AirElemental();
        harness.setLibrary(player1, List.of(chosen, bottomOne, bottomTwo, bottomThree, bottomFour));
        harness.setHand(player1, List.of(new CollectorsCage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent cage = findPermanent(player1, "Collector's Cage");
        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(cage.getCard())).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(bottomOne, bottomTwo, bottomThree, bottomFour);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts a counter on the target and offers the imprinted card when Coven is met")
    void counterAndFreePlayWithCoven() {
        Card imprinted = new GrizzlyBears();
        addCageWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(imprinted.getId())).isNull();
    }

    @Test
    @DisplayName("Still puts on the counter but does not offer the card when Coven is not met")
    void counterWithoutCovenDoesNotPlayCard() {
        Card imprinted = new GrizzlyBears();
        addCageWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        addCageWithImprint(new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
