package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BorosCharm.class, ChandraNalaar.class, GrizzlyBears.class, Murder.class, Naturalize.class,
        Spellbook.class})
class BorosCharmTest extends BaseCardTest {

    // Mode indices: 0 = 4 damage to player/planeswalker, 1 = permanents you control gain
    //               indestructible, 2 = target creature gains double strike.

    @Test
    @DisplayName("Mode 0 deals 4 damage to target player")
    void modeZeroBurnsPlayer() {
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Mode 0 deals 4 damage to target planeswalker")
    void modeZeroBurnsPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 1 makes a creature you control survive destruction")
    void modeOneSavesCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 1 also protects noncreature permanents you control")
    void modeOneSavesNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, spellbookId);

        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Mode 1 indestructible wears off at end of turn")
    void modeOneWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Mode 2 grants double strike to target creature")
    void modeTwoGrantsDoubleStrike() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, 2, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Mode 2 cannot target a noncreature permanent")
    void modeTwoRejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, spellbookId))
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Damage mode can target its controller")
    void modeZeroCanBurnController() {
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage mode cannot target a creature")
    void modeZeroRejectsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Indestructible affects only controlled permanents present at resolution")
    void modeOneDoesNotProtectOpponentsOrLaterArrivals() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        Permanent later = harness.enterBattlefieldAndReturn(player1, new Spellbook());

        assertThat(own.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opponent.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(later.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Double strike can target an opposing creature and lasts until cleanup")
    void modeTwoTargetsOpponentAndWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 2, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
