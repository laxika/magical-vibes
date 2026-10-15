package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.g.GoblinElectromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({InfantryShield.class, GoblinElectromancer.class, InvasionOfZendikar.class,
        AwakenedSkyclave.class, ChandraHopesBeacon.class})
class InfantryShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has menace and mobilizes Warriors equal to its power")
    void equippedCreatureGainsMenaceAndMobilize() {
        Permanent creature = addCreatureReady(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = warriorTokens(player1);
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Mobilized Warrior tokens are sacrificed at the next end step")
    void mobilizedTokensAreSacrificedAtNextEndStep() {
        Permanent creature = addCreatureReady(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(warriorTokens(player1)).hasSize(2);

        advanceToEndStep(player1);

        assertThat(warriorTokens(player1)).isEmpty();
    }

    @Test
    void equipAttachesForTwoManaAndMovesGrantedAbilities() {
        Permanent shield = addShieldReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        resolveAllTriggers();

        assertThat(shield.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, second.getId());
        resolveAllTriggers();

        assertThat(shield.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(warriorTokens(player1)).hasSize(2);
    }

    @Test
    void unattachedShieldDoesNotGrantMobilizeOrMenace() {
        Permanent creature = addCreatureReady(player1);
        addShieldReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(warriorTokens(player1)).isEmpty();
    }

    @Test
    void mobilizeUsesPowerWhenTheTriggerResolves() {
        Permanent creature = addCreatureReady(player1);
        addShieldReady(player1).setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(warriorTokens(player1)).hasSize(5);
    }

    @Test
    void zeroPowerCreatesNoWarriors() {
        Permanent creature = addCreatureReady(player1);
        creature.setPowerModifier(-2);
        addShieldReady(player1).setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(warriorTokens(player1)).isEmpty();
        advanceToEndStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void pendingMobilizeAndDelayedSacrificeSurviveShieldLeaving() {
        Permanent creature = addCreatureReady(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(shield);
        gd.playerGraveyards.get(player1.getId()).add(shield.getCard());
        resolveAllTriggers();

        assertThat(warriorTokens(player1)).hasSize(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();

        advanceToEndStep(player1);

        assertThat(warriorTokens(player1)).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void warriorsCanChooseALegalBattleAsTheirAttackDestination() {
        Permanent creature = addCreatureReady(player1);
        addShieldReady(player1).setAttachedTo(creature.getId());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(battle.getId());

        harness.handlePermanentChosen(player1, battle.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(warriorTokens(player1)).hasSize(2);
        assertThat(warriorTokens(player1)).extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(battle.getId(), player2.getId());
    }

    @Test
    void choosingAttackDestinationsStillSacrificesWarriorsAtTheNextEndStep() {
        Permanent creature = addCreatureReady(player1);
        addShieldReady(player1).setAttachedTo(creature.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(warriorTokens(player1)).hasSize(2);
        assertThat(warriorTokens(player1)).extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(planeswalker.getId(), player2.getId());

        advanceToEndStep(player1);

        assertThat(warriorTokens(player1)).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GoblinElectromancer());
    }

    private Permanent addShieldReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new InfantryShield());
    }

    private List<Permanent> warriorTokens(Player player) {
        return findPermanents(player, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
