package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.FaerieHarbinger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NectarFaerieTest extends BaseCardTest {

    private void addNectarFaerieReady() {
        harness.addToBattlefield(player1, new NectarFaerie());
        Permanent faerie = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nectar Faerie"))
                .findFirst().orElseThrow();
        faerie.setSummoningSick(false);
    }

    private Permanent find(java.util.UUID ownerId, String name) {
        return gd.playerBattlefields.get(ownerId).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Grants lifelink to a target Faerie")
    void grantsLifelinkToFaerie() {
        addNectarFaerieReady();
        harness.addToBattlefield(player1, new FaerieHarbinger());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Faerie Harbinger");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(find(player1.getId(), "Faerie Harbinger").hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Grants lifelink to a target Elf")
    void grantsLifelinkToElf() {
        addNectarFaerieReady();
        harness.addToBattlefield(player1, new ElvishVisionary());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Elvish Visionary");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(find(player1.getId(), "Elvish Visionary").hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Lifelink wears off at end of turn")
    void lifelinkWearsOff() {
        addNectarFaerieReady();
        harness.addToBattlefield(player1, new ElvishVisionary());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Elvish Visionary");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(find(player1.getId(), "Elvish Visionary").hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is neither Faerie nor Elf")
    void cannotTargetNonFaerieNonElf() {
        addNectarFaerieReady();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
