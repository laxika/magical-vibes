package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.r.RaidersKarve;
import com.github.laxika.magicalvibes.cards.k.KayaTheInexorable;
import com.github.laxika.magicalvibes.cards.s.ScornEffigy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JarlOfTheForsaken.class, KayaTheInexorable.class, ScornEffigy.class,
        FrostBite.class, RaidersKarve.class})
class JarlOfTheForsakenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a damaged creature an opponent controls")
    void etbDestroysDamagedOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornEffigy());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Scorn Effigy");
        harness.assertInGraveyard(player2, "Scorn Effigy");
    }

    @Test
    @DisplayName("ETB destroys a damaged planeswalker an opponent controls")
    void etbDestroysDamagedOpponentPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KayaTheInexorable());
        target.setCounterCount(CounterType.LOYALTY, 3);
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Kaya the Inexorable");
        harness.assertInGraveyard(player2, "Kaya the Inexorable");
    }

    @Test
    @DisplayName("Cannot target an opponent permanent that was not dealt damage this turn")
    void cannotTargetUndamagedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornEffigy());
        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Can be foretold and cast from exile later")
    void foretellsAndCastsFromExile() {
        JarlOfTheForsaken jarl = new JarlOfTheForsaken();
        harness.setHand(player1, List.of(jarl));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(jarl.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornEffigy());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, jarl.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jarl of the Forsaken");
    }

    @Test
    void cannotTargetDamagedCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ScornEffigy());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Scorn Effigy");
    }

    @Test
    void cannotTargetDamagedNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaidersKarve());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Raiders' Karve");
    }

    @Test
    void canEnterWithoutAnyLegalEtbTarget() {
        harness.addToBattlefield(player2, new ScornEffigy());
        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Jarl of the Forsaken");
        harness.assertOnBattlefield(player2, "Scorn Effigy");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void flashDestroysCreatureDamagedBySpellDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornEffigy());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new FrostBite(), new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertOnBattlefield(player2, "Scorn Effigy");
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Jarl of the Forsaken");
        harness.assertInGraveyard(player2, "Scorn Effigy");
    }

    @Test
    void destroysPlaneswalkerAfterActualSpellDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KayaTheInexorable());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new FrostBite(), new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Kaya the Inexorable");
    }

    @Test
    void cannotCastForetoldCardOnTheTurnItWasExiled() {
        JarlOfTheForsaken jarl = new JarlOfTheForsaken();
        harness.setHand(player1, List.of(jarl));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, jarl.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(jarl.getId())).isNotNull();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
        harness.assertInHand(player1, "Jarl of the Forsaken");
    }

    @Test
    void etbDoesNotDestroyTargetThatYouGainControlOfBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornEffigy());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new JarlOfTheForsaken()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Jarl of the Forsaken");
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Scorn Effigy");
        harness.assertNotInGraveyard(player2, "Scorn Effigy");
    }

    @Test
    void foretoldCardCanBeCastDuringOpponentsLaterTurn() {
        JarlOfTheForsaken jarl = new JarlOfTheForsaken();
        harness.setHand(player1, List.of(jarl));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornEffigy());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromExile(player1, jarl.getId(), target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Jarl of the Forsaken");
        harness.assertInGraveyard(player2, "Scorn Effigy");
    }
}
