package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevourerOfMemory.class, Forest.class, GrizzlyBears.class, Millstone.class})
class DevourerOfMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Milling a card gives Devourer of Memory +1/+1 and makes it unblockable")
    void millingCardBoostsAndMakesUnblockable() {
        Permanent devourer = addCreatureReady(player1, new DevourerOfMemory());
        harness.setLibrary(player1, List.of(new Forest()));
        activateMillAbility();

        assertThat(devourer.getEffectivePower()).isEqualTo(3);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(2);
        assertThat(devourer.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Milling multiple cards in one event triggers only once")
    void multipleCardsInOneMillEventTriggerOnce() {
        Permanent devourer = addCreatureReady(player1, new DevourerOfMemory());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(devourer.getEffectivePower()).isEqualTo(3);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost and unblockable effect wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent devourer = addCreatureReady(player1, new DevourerOfMemory());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        activateMillAbility();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(devourer.getEffectivePower()).isEqualTo(2);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(1);
        assertThat(devourer.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Separate mill events each boost Devourer of Memory")
    void separateMillEventsStackBoosts() {
        Permanent devourer = addCreatureReady(player1, new DevourerOfMemory());
        harness.setLibrary(player1, List.of(new DevourerOfMemory(), new DevourerOfMemory()));

        activateMillAbility();
        activateMillAbility();

        assertThat(devourer.getEffectivePower()).isEqualTo(4);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(3);
        assertThat(devourer.isCantBeBlocked()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not trigger the boost")
    void emptyLibraryDoesNotTrigger() {
        Permanent devourer = addCreatureReady(player1, new DevourerOfMemory());
        harness.setLibrary(player1, List.of());

        activateMillAbility();

        assertThat(devourer.getEffectivePower()).isEqualTo(2);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(1);
        assertThat(devourer.isCantBeBlocked()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling the opponent's library boosts only their Devourer")
    void opponentMillDoesNotBoostYourDevourer() {
        Permanent ownDevourer = addCreatureReady(player1, new DevourerOfMemory());
        Permanent opposingDevourer = addCreatureReady(player2, new DevourerOfMemory());
        harness.setLibrary(player2, List.of(new DevourerOfMemory()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(ownDevourer.getEffectivePower()).isEqualTo(2);
        assertThat(ownDevourer.getEffectiveToughness()).isEqualTo(1);
        assertThat(ownDevourer.isCantBeBlocked()).isFalse();
        assertThat(opposingDevourer.getEffectivePower()).isEqualTo(3);
        assertThat(opposingDevourer.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingDevourer.isCantBeBlocked()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The mill ability works while tapped and summoning sick and mills only the top card")
    void millAbilityNeedsNoTapAndMillsOneCard() {
        harness.addToBattlefield(player1, new DevourerOfMemory());
        Permanent devourer = gd.playerBattlefields.get(player1.getId()).getFirst();
        devourer.setSummoningSick(true);
        devourer.setTapped(true);
        DevourerOfMemory topCard = new DevourerOfMemory();
        DevourerOfMemory nextCard = new DevourerOfMemory();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        activateMillAbility();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(devourer.getEffectivePower()).isEqualTo(3);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(2);
        assertThat(devourer.isCantBeBlocked()).isTrue();
        assertThat(devourer.isTapped()).isTrue();
    }

    private void activateMillAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
    }
}
