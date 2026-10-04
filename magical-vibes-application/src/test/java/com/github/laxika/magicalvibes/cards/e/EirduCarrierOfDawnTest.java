package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EirduCarrierOfDawn.class, GrizzlyBears.class, DoomBlade.class})
class EirduCarrierOfDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells can be cast using convoke while Eirdu is on the battlefield")
    void grantsConvokeToCreatureSpells() {
        addFrontFace(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(2);
    }

    @Test
    void doesNotGrantConvokeToOpponentsCreatureSpells() {
        addFrontFace(player2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void doesNotGrantConvokeToNoncreatureSpells() {
        addFrontFace(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, creature.getId(), null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void backFaceDoesNotGrantConvoke() {
        addBackFace(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Transforms into Isilu after paying black in the first main phase")
    void transformsToBackFaceAfterPayingBlack() {
        Permanent eirdu = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(eirdu.isTransformed()).isTrue();
        assertThat(eirdu.getCard().getName()).isEqualTo("Isilu, Carrier of Twilight");
    }

    @Test
    @DisplayName("Transforms back into Eirdu after paying white in the first main phase")
    void transformsToFrontFaceAfterPayingWhite() {
        Permanent isilu = addBackFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(isilu.isTransformed()).isFalse();
        assertThat(isilu.getCard().getName()).isEqualTo("Eirdu, Carrier of Dawn");
    }

    @Test
    @DisplayName("Other nontoken creatures you control have persist on Isilu")
    void grantsPersistToOtherNontokenCreatures() {
        Permanent isilu = addBackFace(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent token = addTokenCreature(player1);

        assertThat(gqs.hasKeyword(gd, isilu, Keyword.PERSIST)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.PERSIST)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Persist returns another nontoken creature with a -1/-1 counter")
    void persistReturnsOtherNontokenCreature() {
        addBackFace(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        resolveUntilInputOrEmpty();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningBlackPaymentKeepsFrontFace() {
        Permanent eirdu = addFrontFace(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(eirdu.isTransformed()).isFalse();
    }

    @Test
    void decliningWhitePaymentKeepsBackFace() {
        Permanent isilu = addBackFace(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(isilu.isTransformed()).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsFirstMainPhase() {
        Permanent eirdu = addFrontFace(player1);
        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(eirdu.isTransformed()).isFalse();
    }

    @Test
    void doesNotGrantPersistToOpponentsCreatures() {
        addBackFace(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        resolveUntilInputOrEmpty();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void creatureWithMinusOneCounterDoesNotPersist() {
        addBackFace(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        resolveUntilInputOrEmpty();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed(SafeholdElite.class)
    void grantedPersistTriggersSeparatelyFromPrintedPersist() {
        addBackFace(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Safehold Elite");
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @CardUsed(DayOfJudgment.class)
    void persistStillReturnsCreatureWhenIsiluDiesSimultaneously() {
        addBackFace(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        resolveUntilInputOrEmpty();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() instanceof EirduCarrierOfDawn);
    }

    private Permanent addFrontFace(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new EirduCarrierOfDawn());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addBackFace(Player player) {
        EirduCarrierOfDawn card = new EirduCarrierOfDawn();
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private Permanent addTokenCreature(Player player) {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
