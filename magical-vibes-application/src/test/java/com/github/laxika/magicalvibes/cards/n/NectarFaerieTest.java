package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.FaerieHarbinger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NectarFaerie.class, FaerieHarbinger.class, ElvishVisionary.class, GrizzlyBears.class, AmoeboidChangeling.class})
class NectarFaerieTest extends BaseCardTest {

    private void addNectarFaerieReady() {
        addCreatureReady(player1, new NectarFaerie());
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

        assertThat(findPermanent(player1, "Faerie Harbinger").hasKeyword(Keyword.LIFELINK)).isTrue();
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

        assertThat(findPermanent(player1, "Elvish Visionary").hasKeyword(Keyword.LIFELINK)).isTrue();
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

        assertThat(findPermanent(player1, "Elvish Visionary").hasKeyword(Keyword.LIFELINK)).isFalse();
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

    @Test
    @DisplayName("Can target itself and pays one black mana and taps")
    void canTargetItselfAndPaysCosts() {
        Permanent faerie = addCreatureReady(player1, new NectarFaerie());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, faerie.getId());

        assertThat(faerie.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(faerie.hasKeyword(Keyword.LIFELINK)).isFalse();
        harness.passBothPriorities();
        assertThat(faerie.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new NectarFaerie());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faerie.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(faerie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent faerie = addCreatureReady(player1, new NectarFaerie());
        faerie.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faerie.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the black cost with colorless mana")
    void cannotPayWithColorlessMana() {
        Permanent faerie = addCreatureReady(player1, new NectarFaerie());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faerie.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(faerie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can grant lifelink to a changeling")
    void canTargetChangeling() {
        addNectarFaerieReady();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("An opposing Faerie gains life for its own controller")
    void opposingFaerieGainsLifeForItsController() {
        addNectarFaerieReady();
        Permanent target = addCreatureReady(player2, new FaerieHarbinger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Ability still resolves after Nectar Faerie leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        addNectarFaerieReady();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FaerieHarbinger());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not grant lifelink when the target loses its qualifying types in response")
    void targetLosingTypesMakesAbilityFizzle() {
        addNectarFaerieReady();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FaerieHarbinger());
        addCreatureReady(player2, new AmoeboidChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
