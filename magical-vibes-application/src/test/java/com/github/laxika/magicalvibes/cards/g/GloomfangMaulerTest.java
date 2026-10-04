package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.e.EtchedFamiliar;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloomfangMauler.class, EtchedFamiliar.class, Swamp.class})
class GloomfangMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts two counters on another creature and grants menace")
    void backsUpAnotherCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EtchedFamiliar());
        Permanent mauler = castGloomfangMauler();

        resolveEtbTargeting(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counters without granting menace")
    void backingUpSourceDoesNotGrantMenace() {
        Permanent mauler = castGloomfangMauler();

        resolveEtbTargeting(mauler);

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(mauler.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("Backup's granted menace expires at the end of the turn")
    void grantedMenaceExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EtchedFamiliar());
        castGloomfangMauler();
        resolveEtbTargeting(creature);

        assertThat(creature.hasKeyword(Keyword.MENACE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Swampcycling discards the card and searches for a Swamp")
    void swampcyclingSearchesForSwamp() {
        harness.setHand(player1, List.of(new GloomfangMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Swamp(), new EtchedFamiliar()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gloomfang Mauler");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getSubtypes().contains(CardSubtype.SWAMP))
                .hasSize(1);
    }

    @Test
    @DisplayName("Backup can give an opponent's creature counters and menace")
    void backsUpOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EtchedFamiliar());
        castGloomfangMauler();

        resolveEtbTargeting(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Swampcycling puts the selected Swamp into hand without drawing another card")
    void swampcyclingCompletesSearch() {
        harness.setHand(player1, List.of(new GloomfangMauler()));
        harness.setLibrary(player1, List.of(new Swamp(), new EtchedFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Gloomfang Mauler");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Swampcycling resolves when the library contains no Swamp")
    void swampcyclingWithoutSwamp() {
        EtchedFamiliar nonSwamp = new EtchedFamiliar();
        harness.setHand(player1, List.of(new GloomfangMauler()));
        harness.setLibrary(player1, List.of(nonSwamp));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gloomfang Mauler");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonSwamp);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castGloomfangMauler() {
        harness.setHand(player1, List.of(new GloomfangMauler()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GloomfangMauler)
                .findFirst()
                .orElseThrow();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
