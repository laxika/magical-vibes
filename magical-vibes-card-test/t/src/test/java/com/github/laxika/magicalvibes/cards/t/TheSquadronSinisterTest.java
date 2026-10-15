package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.s.ScarletWitchWandaMaximoff;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSquadronSinister.class, DocOcksHenchmen.class, ScarletWitchWandaMaximoff.class})
class TheSquadronSinisterTest extends BaseCardTest {

    @Test
    @DisplayName("Other Villains you control get +2/+2 and flying and haste")
    void buffsOtherVillainsYouControl() {
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DocOcksHenchmen());
        int basePower = gqs.getEffectivePower(gd, villain);
        int baseToughness = gqs.getEffectiveToughness(gd, villain);

        harness.addToBattlefield(player1, new TheSquadronSinister());

        assertThat(gqs.getEffectivePower(gd, villain)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, villain)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, villain, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff non-Villains or the opponent's Villains")
    void onlyBuffsOtherVillainsYouControl() {
        Permanent nonVillain = harness.addToBattlefieldAndReturn(player1, new ScarletWitchWandaMaximoff());
        int nonVillainBasePower = gqs.getEffectivePower(gd, nonVillain);
        Permanent opponentVillain = harness.addToBattlefieldAndReturn(player2, new DocOcksHenchmen());
        int opponentVillainBasePower = gqs.getEffectivePower(gd, opponentVillain);

        harness.addToBattlefield(player1, new TheSquadronSinister());

        assertThat(gqs.getEffectivePower(gd, nonVillain)).isEqualTo(nonVillainBasePower);
        assertThat(gqs.hasKeyword(gd, nonVillain, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentVillain)).isEqualTo(opponentVillainBasePower);
        assertThat(gqs.hasKeyword(gd, opponentVillain, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Mayhem casts The Squadron Sinister from the graveyard after it was discarded this turn")
    void mayhemCastsAfterDiscarding() {
        TheSquadronSinister card = new TheSquadronSinister();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getOriginalCard() == card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Mayhem cannot cast The Squadron Sinister before it was discarded this turn")
    void mayhemRequiresDiscardThisTurn() {
        harness.setGraveyard(player1, List.of(new TheSquadronSinister()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The anthem does not boost its own source")
    void doesNotBoostItself() {
        TheSquadronSinister card = new TheSquadronSinister();
        Permanent source = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(card.getPower());
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(card.getToughness());
    }

    @Test
    @DisplayName("Villains lose the anthem's bonuses when its source leaves")
    void bonusesEndWhenSourceLeaves() {
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DocOcksHenchmen());
        int basePower = gqs.getEffectivePower(gd, villain);
        int baseToughness = gqs.getEffectiveToughness(gd, villain);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TheSquadronSinister());
        assertThat(gqs.getEffectivePower(gd, villain)).isEqualTo(basePower + 2);
        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, villain, Keyword.HASTE)).isTrue();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);

        assertThat(gqs.getEffectivePower(gd, villain)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, villain)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, villain, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Mayhem does not allow casting during combat")
    void mayhemPreservesSorceryTiming() {
        TheSquadronSinister card = new TheSquadronSinister();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mayhem requires the red mana in its alternative cost")
    void mayhemRequiresCorrectColoredMana() {
        TheSquadronSinister card = new TheSquadronSinister();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding another card does not enable this card's Mayhem")
    void mayhemRequiresThisSpecificCardToBeDiscarded() {
        TheSquadronSinister card = new TheSquadronSinister();
        DocOcksHenchmen discarded = new DocOcksHenchmen();
        harness.setGraveyard(player1, List.of(card, discarded));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(discarded.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Mayhem cannot reuse a discard after the card was cast and died")
    void mayhemDoesNotSurviveLeavingTheGraveyard() {
        TheSquadronSinister card = new TheSquadronSinister();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == card)
                .findFirst().orElseThrow();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
