package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GruulScrapper;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MizziumTransreliquat.class, IzzetSignet.class, GruulSignet.class, GruulScrapper.class})
class MizziumTransreliquatTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a copy of target artifact until end of turn")
    void becomesCopyOfTargetArtifactUntilEndOfTurn() {
        Permanent transreliquat = harness.addToBattlefieldAndReturn(player1, new MizziumTransreliquat());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, signet.getId());
        harness.passBothPriorities();

        assertThat(transreliquat.getCard().getName()).isEqualTo("Izzet Signet");

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(transreliquat.getCard()).isSameAs(transreliquat.getOriginalCard());
    }

    @Test
    @DisplayName("The copy-with-exception ability remains available on the copy")
    void copyWithExceptionRetainsAbility() {
        Permanent transreliquat = harness.addToBattlefieldAndReturn(player1, new MizziumTransreliquat());
        Permanent izzetSignet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        Permanent gruulSignet = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, izzetSignet.getId());
        harness.passBothPriorities();
        assertThat(transreliquat.getCard().getName()).isEqualTo("Izzet Signet");

        harness.activateAbility(player1, 0, 1, null, gruulSignet.getId());
        harness.passBothPriorities();

        assertThat(transreliquat.getCard().getName()).isEqualTo("Gruul Signet");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new MizziumTransreliquat());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The copy-with-exception ability cannot target a creature")
    void copyWithExceptionCannotTargetCreature() {
        harness.addToBattlefield(player1, new MizziumTransreliquat());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GruulScrapper());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The copy-with-exception effect survives cleanup and retains its ability")
    void permanentCopySurvivesCleanup() {
        Permanent transreliquat = harness.addToBattlefieldAndReturn(player1, new MizziumTransreliquat());
        Permanent izzetSignet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        Permanent gruulSignet = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, izzetSignet.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(transreliquat.getCard().getName()).isEqualTo("Izzet Signet");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, gruulSignet.getId());
        harness.passBothPriorities();

        assertThat(transreliquat.getCard().getName()).isEqualTo("Gruul Signet");
    }

    @Test
    @DisplayName("A temporary copy on top of a permanent copy reverts to the permanent copy")
    void temporaryCopyRevealsUnderlyingPermanentCopy() {
        Permanent transreliquat = harness.addToBattlefieldAndReturn(player1, new MizziumTransreliquat());
        Permanent izzetSignet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        Permanent gruulSignet = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, gruulSignet.getId());
        harness.activateAbility(player1, 0, 1, null, izzetSignet.getId());
        harness.passBothPriorities();
        assertThat(transreliquat.getCard().getName()).isEqualTo("Izzet Signet");
        harness.passBothPriorities();
        assertThat(transreliquat.getCard().getName()).isEqualTo("Gruul Signet");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(transreliquat.getCard().getName()).isEqualTo("Izzet Signet");
    }

    @Test
    @DisplayName("The first ability may copy itself and then copy another artifact")
    void selfCopyRetainsOriginalAbilities() {
        Permanent transreliquat = harness.addToBattlefieldAndReturn(player1, new MizziumTransreliquat());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, transreliquat.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, signet.getId());
        harness.passBothPriorities();

        assertThat(transreliquat.getCard().getName()).isEqualTo("Izzet Signet");
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(transreliquat.getCard()).isSameAs(transreliquat.getOriginalCard());
    }

    @Test
    @DisplayName("A temporary copy loses the original copy-with-exception ability")
    void temporaryCopyLosesCopyAbility() {
        harness.addToBattlefield(player1, new MizziumTransreliquat());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, signet.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
