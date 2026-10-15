package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.ManaReflection;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.s.SilkbindFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DroveOfElves.class, DevotedDruid.class, SafeholdElite.class, SilkbindFaerie.class,
        ManaReflection.class})
class DroveOfElvesTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a green permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent drove = addDrove(player1);

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of green permanents you control")
    void ptEqualsGreenPermanentCount() {
        Permanent drove = addDrove(player1);
        addCreatureReady(player1, new DevotedDruid());
        addCreatureReady(player1, new SafeholdElite());

        // itself + Devoted Druid + Safehold Elite = 3
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-green permanents you control are not counted")
    void ignoresNonGreenPermanents() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player1, new SilkbindFaerie()); // blue and white

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only your green permanents, not the opponent's")
    void ignoresOpponentGreenPermanents() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player2, new DevotedDruid());

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when green permanents change")
    void ptUpdatesWhenGreenPermanentsChange() {
        Permanent drove = addDrove(player1);
        Permanent devotedDruid = addCreatureReady(player1, new DevotedDruid());
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(devotedDruid);
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts a green noncreature permanent")
    void countsGreenNoncreaturePermanent() {
        Permanent drove = addDrove(player1);
        harness.addToBattlefield(player1, new ManaReflection());

        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hexproof prevents an opponent's activated ability from targeting Drove")
    void opponentCannotTargetWithAbility() {
        Permanent drove = addDrove(player1);
        Permanent faerie = addCreatureReady(player2, new SilkbindFaerie());
        faerie.tap();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, drove.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(drove.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Hexproof permits its controller's activated ability to target Drove")
    void controllerCanTargetWithAbility() {
        Permanent drove = addDrove(player1);
        Permanent faerie = addCreatureReady(player1, new SilkbindFaerie());
        faerie.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, drove.getId());
        harness.passBothPriorities();

        assertThat(drove.isTapped()).isTrue();
        assertThat(faerie.isTapped()).isFalse();
    }

    @Test
    @DisplayName("In hand, P/T counts green permanents without counting the card itself")
    void characteristicAbilityWorksInHand() {
        DroveOfElves drove = new DroveOfElves();
        harness.setHand(player1, List.of(drove));
        harness.addToBattlefield(player2, new DevotedDruid());

        assertThat(gqs.getEffectiveCardPower(gd, drove)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, drove)).isZero();

        harness.addToBattlefield(player1, new SafeholdElite());
        harness.addToBattlefield(player1, new ManaReflection());

        assertThat(gqs.getEffectiveCardPower(gd, drove)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, drove)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves with P/T counting itself and other green permanents")
    void resolvesWithDynamicPowerAndToughness() {
        harness.addToBattlefield(player1, new ManaReflection());
        harness.castFromHand(player1, new DroveOfElves(), "{3}{G}");
        harness.passBothPriorities();

        Permanent drove = findPermanent(player1, "Drove of Elves");
        assertThat(drove).isNotNull();
        assertThat(gqs.getEffectivePower(gd, drove)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drove)).isEqualTo(2);
    }

    private Permanent addDrove(Player player) {
        return addCreatureReady(player, new DroveOfElves());
    }
}
