package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NayaCharm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HardenedAcademicTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants lifelink until end of turn")
    void discardingCardGrantsLifelink() {
        harness.addToBattlefield(player1, new HardenedAcademic());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, battlefieldIndex(player1, "Hardened Academic"), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent academic = findPermanent(player1, "Hardened Academic");
        assertThat(gqs.hasKeyword(gd, academic, Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Lifelink granted by the ability wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HardenedAcademic());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, battlefieldIndex(player1, "Hardened Academic"), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent academic = findPermanent(player1, "Hardened Academic");
        assertThat(gqs.hasKeyword(gd, academic, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cards leaving your graveyard put a counter on a target creature you control")
    void cardsLeavingGraveyardPutCounterOnTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new HardenedAcademic());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, card.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(harness.getPermanentId(player1, "Hardened Academic"), targetId)
                .doesNotContain(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId))
                .findFirst()
                .orElseThrow();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The discard ability cannot be activated with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new HardenedAcademic());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Hardened Academic"), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cards leaving an opponent's graveyard do not trigger the ability")
    void opponentGraveyardDoesNotTrigger() {
        Permanent academic = addAcademic(player1);
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new NayaCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, card.getId());
        harness.passBothPriorities();

        assertThat(academic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addAcademic(Player player) {
        harness.addToBattlefield(player, new HardenedAcademic());
        return findPermanent(player, "Hardened Academic");
    }

    private int battlefieldIndex(Player player, String cardName) {
        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }
}
