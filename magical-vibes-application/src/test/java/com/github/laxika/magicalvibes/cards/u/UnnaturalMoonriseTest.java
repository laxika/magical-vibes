package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnnaturalMoonrise.class, DawnhartRejuvenator.class, Forest.class})
class UnnaturalMoonriseTest extends BaseCardTest {

    @Test
    void grantedDrawAbilityExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new UnnaturalMoonrise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void illegalTargetPreventsNightAndExilesFlashbackSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Card spell = new UnnaturalMoonrise();
        gd.dayNight = DayNight.DAY;
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Unnatural Moonrise");
    }

    @Test
    void opponentDrawsForAbilityGrantedToTheirCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        creature.setSummoningSick(false);
        harness.setHand(player1, List.of(new UnnaturalMoonrise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.setLibrary(player2, List.of(new Forest()));
        int casterHandSize = gd.playerHands.get(player1.getId()).size();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        creature.setAttacking(true);
        harness.setLife(player1, 20);
        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(casterHandSize);
    }

    @Test
    void repeatedCastsGrantSeparateDrawAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        creature.setSummoningSick(false);
        harness.setHand(player1, List.of(new UnnaturalMoonrise(), new UnnaturalMoonrise()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        creature.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    @DisplayName("Makes it night and grants the target creature the printed temporary effects")
    void makesItNightAndBuffsTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new UnnaturalMoonrise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Draws a card when the target creature deals combat damage to a player")
    void drawsOnTargetCombatDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        bears.setSummoningSick(false);
        harness.setHand(player1, List.of(new UnnaturalMoonrise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        bears.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Flashback applies the effect and exiles the spell")
    void flashbackAppliesEffectAndExilesSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Card spell = new UnnaturalMoonrise();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, bears.getId());

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        harness.assertNotInGraveyard(player1, "Unnatural Moonrise");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("The temporary creature effects wear off at end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new UnnaturalMoonrise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new UnnaturalMoonrise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
