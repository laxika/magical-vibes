package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ExhibitionMagician;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Goldhound.class, ExhibitionMagician.class})
class GoldhoundTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Goldhound adds one mana of the chosen color")
    void activatingAddsChosenColorMana() {
        Permanent goldhound = addCreatureReady(player1, new Goldhound());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goldhound);
        harness.assertInGraveyard(player1, "Goldhound");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canProduceEachColorAndPaysSacrificeBeforeChoosing(ManaColor color) {
        addCreatureReady(player1, new Goldhound());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Goldhound");
        harness.assertInGraveyard(player1, "Goldhound");
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void summoningSicknessPreventsActivationWithoutSacrificing() {
        Permanent goldhound = harness.addToBattlefieldAndReturn(player1, new Goldhound());
        goldhound.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Goldhound");
        harness.assertNotInGraveyard(player1, "Goldhound");
        assertThat(goldhound.isTapped()).isFalse();
    }

    @Test
    void tappedGoldhoundCannotActivate() {
        Permanent goldhound = addCreatureReady(player1, new Goldhound());
        goldhound.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Goldhound");
        harness.assertNotInGraveyard(player1, "Goldhound");
    }

    @Test
    void menaceRejectsOneBlockerButAllowsTwo() {
        addCreatureReady(player1, new Goldhound());
        addCreatureReady(player2, new Goldhound());
        addCreatureReady(player2, new Goldhound());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    @Test
    void firstStrikeKillsAttackerBeforeItCanDealDamage() {
        addCreatureReady(player1, new ExhibitionMagician());
        addCreatureReady(player2, new Goldhound());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Exhibition Magician");
        harness.assertOnBattlefield(player2, "Goldhound");
        harness.assertLife(player2, 20);
    }
}
