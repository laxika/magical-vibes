package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({CreepingTarPit.class})
class CreepingTarPitTest extends BaseCardTest {

    @Test
    @DisplayName("Creeping Tar Pit enters tapped and adds blue or black mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new CreepingTarPit()));
        harness.playLand(player1, 0);

        Permanent tarPit = findPermanent(player1, "Creeping Tar Pit");
        assertThat(tarPit.isTapped()).isTrue();

        tarPit.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Creeping Tar Pit becomes an unblockable 3/2 blue and black Elemental")
    void animatesAndBecomesUnblockable() {
        Permanent tarPit = addCreatureReady(player1, new CreepingTarPit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tarPit)).isTrue();
        assertThat(gqs.isLand(gd, tarPit)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tarPit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tarPit)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, tarPit))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(tarPit.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(tarPit.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Creeping Tar Pit's animation and unblockability end at end of turn")
    void animationAndUnblockabilityEndAtEndOfTurn() {
        Permanent tarPit = addCreatureReady(player1, new CreepingTarPit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tarPit)).isFalse();
        assertThat(gqs.isLand(gd, tarPit)).isTrue();
        assertThat(tarPit.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
        assertThat(tarPit.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Creeping Tar Pit can produce blue mana without using the stack")
    void addsBlueManaImmediately() {
        Permanent tarPit = addCreatureReady(player1, new CreepingTarPit());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(tarPit.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An animated Creeping Tar Pit retains its mana ability")
    void retainsManaAbilityWhileAnimated() {
        Permanent tarPit = addCreatureReady(player1, new CreepingTarPit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(tarPit.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(tarPit.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, tarPit)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creeping Tar Pit can animate repeatedly while tapped without animating other lands")
    void canAnimateRepeatedlyWhileTapped() {
        Permanent tarPit = addCreatureReady(player1, new CreepingTarPit());
        Permanent otherTarPit = addCreatureReady(player1, new CreepingTarPit());
        tarPit.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(tarPit.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, tarPit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tarPit)).isEqualTo(2);
        assertThat(tarPit.isCantBeBlocked()).isTrue();
        assertThat(gqs.isCreature(gd, otherTarPit)).isFalse();
        assertThat(otherTarPit.isCantBeBlocked()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A newly played Creeping Tar Pit can animate but cannot tap for mana as a creature")
    void animationDoesNotGrantHaste() {
        harness.setHand(player1, List.of(new CreepingTarPit()));
        harness.playLand(player1, 0);
        Permanent tarPit = findPermanent(player1, "Creeping Tar Pit");
        tarPit.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tarPit)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(tarPit.isTapped()).isFalse();
    }


}
