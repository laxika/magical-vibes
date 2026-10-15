package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellBorcaSpectralSergeant.class, SolRing.class, DressDown.class})
class BellBorcaSpectralSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Power starts at zero and toughness is five")
    void startsAtZeroPowerAndFiveToughness() {
        Permanent bell = addReadyBell(player1);

        assertThat(gqs.getEffectivePower(gd, bell)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bell)).isEqualTo(5);
    }

    @Test
    @DisplayName("Notes the greatest mana value of cards put into exile this turn")
    void notesGreatestManaValueOfExiledCards() {
        Permanent bell = addReadyBell(player1);
        Card low = cardWithManaCost("Low", "{2}");
        Card high = cardWithManaCost("High", "{5}");

        harness.inMutationScope(() -> {
            gd.addToExile(player2.getId(), low);
            gd.addToExile(player1.getId(), high);
        });

        assertThat(gqs.getEffectivePower(gd, bell)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bell)).isEqualTo(5);
    }

    @Test
    @DisplayName("Upkeep trigger exiles the top card with normal-cost play permission")
    void upkeepExilesTopCardWithNormalCostPermission() {
        Card top = cardWithManaCost("Top", "{4}");
        harness.setLibrary(player1, List.of(top));
        Permanent bell = addReadyBell(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
        assertThat(gqs.getEffectivePower(gd, bell)).isEqualTo(4);
    }

    private Permanent addReadyBell(Player player) {
        return addCreatureReady(player, new BellBorcaSpectralSergeant());
    }

    @Test
    void notingManaValueDoesNotUseTheStack() {
        Permanent bell = addReadyBell(player1);

        harness.inMutationScope(() -> gd.addToExile(player2.getId(), new SolRing()));

        assertThat(gqs.getEffectivePower(gd, bell)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lowerManaValueDoesNotReplaceGreatestNotedValue() {
        Permanent bell = addReadyBell(player1);
        harness.inMutationScope(() -> {
            gd.addToExile(player2.getId(), new BellBorcaSpectralSergeant());
            gd.addToExile(player1.getId(), new SolRing());
        });

        assertThat(gqs.getEffectivePower(gd, bell)).isEqualTo(4);
    }

    @Test
    void faceDownExileHasZeroManaValue() {
        Permanent bell = addReadyBell(player1);
        harness.inMutationScope(() -> gd.addToExile(
                player2.getId(), new BellBorcaSpectralSergeant(), null, true));

        assertThat(gqs.getEffectivePower(gd, bell)).isZero();
    }

    @Test
    void doesNotNoteCardsExiledBeforeItEntered() {
        harness.inMutationScope(() -> gd.addToExile(player2.getId(), new BellBorcaSpectralSergeant()));
        Permanent bell = addReadyBell(player1);

        assertThat(gqs.getEffectivePower(gd, bell)).isZero();
    }

    @Test
    void doesNotNoteManaValuesWhileItsAbilitiesAreRemoved() {
        Permanent bell = addReadyBell(player1);
        harness.addToBattlefield(player1, new DressDown());
        harness.inMutationScope(() -> gd.addToExile(player2.getId(), new BellBorcaSpectralSergeant()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dress Down");
        assertThat(gqs.getEffectivePower(gd, bell)).isZero();
    }

    @Test
    void upkeepCardCanBeCastForItsNormalCost() {
        SolRing top = new SolRing();
        harness.setLibrary(player1, List.of(top));
        Permanent bell = addReadyBell(player1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThat(gqs.getEffectivePower(gd, bell)).isEqualTo(1);
    }

    @Test
    void powerAndUpkeepPlayPermissionResetOnTheNextTurn() {
        SolRing top = new SolRing();
        harness.setLibrary(player1, List.of(top));
        Permanent bell = addReadyBell(player1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bell)).isZero();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
    }

    private Card cardWithManaCost(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        return card;
    }
}
