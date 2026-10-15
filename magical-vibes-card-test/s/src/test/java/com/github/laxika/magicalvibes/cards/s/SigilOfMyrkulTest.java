package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigilOfMyrkul.class, Forest.class, SteadfastPaladin.class})
class SigilOfMyrkulTest extends BaseCardTest {

    @Test
    void millsAndThenBoostsAControlledCreatureWhenThresholdIsReached() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(), new SteadfastPaladin()));
        harness.setLibrary(player1, List.of(new SteadfastPaladin()));

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice interaction =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validPermanentIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotCreateReflexiveAbilityBelowCreatureGraveyardThreshold() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin()));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void doesNotCreateReflexiveAbilityWhenNoCardCanBeMilled() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin(), new SteadfastPaladin()));
        harness.setLibrary(player1, List.of());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void doesNotMillDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin(), new SteadfastPaladin()));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void millsWithoutAControlledCreatureToTarget() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        harness.addToBattlefield(player2, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin()));
        harness.setLibrary(player1, List.of(new SteadfastPaladin()));

        advanceToCombat(player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksCreatureThresholdWhenReflexiveAbilityResolves() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin()));
        harness.setLibrary(player1, List.of(new SteadfastPaladin()));

        advanceToCombat(player1);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.handlePermanentChosen(player1, target.getId()));
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin()));
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void noncreatureCardsAndOpponentsGraveyardDoNotMeetThreshold() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin()));
        harness.setGraveyard(player2, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin(), new SteadfastPaladin()));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void reflexiveAbilityDoesNotAffectATargetThatLeftTheBattlefield() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin()));
        harness.setLibrary(player1, List.of(new SteadfastPaladin()));

        advanceToCombat(player1);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.handlePermanentChosen(player1, target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    void deathtouchExpiresButCounterRemainsAfterCleanup() {
        harness.addToBattlefield(player1, new SigilOfMyrkul());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin(), new SteadfastPaladin(),
                new SteadfastPaladin(), new SteadfastPaladin()));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.handlePermanentChosen(player1, target.getId()));
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);
    }
}
