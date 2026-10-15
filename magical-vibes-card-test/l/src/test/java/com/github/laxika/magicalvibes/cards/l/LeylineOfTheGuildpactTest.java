package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.r.RubblebeltMaverick;
import com.github.laxika.magicalvibes.cards.s.SceneOfTheCrime;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeylineOfTheGuildpact.class, Forest.class, RubblebeltMaverick.class,
        SceneOfTheCrime.class, BloodMoon.class})
class LeylineOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTheGuildpact()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        openingHarness.assertOnBattlefield(openingHarness.getPlayer1(), "Leyline of the Guildpact");
        openingHarness.assertNotInHand(openingHarness.getPlayer1(), "Leyline of the Guildpact");
    }

    @Test
    @DisplayName("Leyline makes your nonland permanents all colors")
    void makesOwnNonlandPermanentsAllColors() {
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfTheGuildpact());
        Permanent maverick = harness.addToBattlefieldAndReturn(player1, new RubblebeltMaverick());
        Permanent opponentMaverick = harness.addToBattlefieldAndReturn(player2, new RubblebeltMaverick());

        assertThat(gqs.getEffectiveColors(gd, leyline))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, maverick))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, opponentMaverick))
                .doesNotContain(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED);
    }

    @Test
    @DisplayName("Leyline makes your lands every basic land type and lets them produce any color")
    void makesOwnLandsEveryBasicTypeAndAnyColorMana() {
        harness.addToBattlefield(player1, new LeylineOfTheGuildpact());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes())
                .contains(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST);
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();

        harness.activateAbility(player1, 1, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Leyline's static abilities end when it leaves the battlefield")
    void staticAbilitiesEndWhenLeylineLeaves() {
        harness.addToBattlefield(player1, new LeylineOfTheGuildpact());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        gd.playerBattlefields.get(player1.getId()).removeFirst();

        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes())
                .doesNotContain(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN);
    }

    @Test
    void openingHandPlacementCanBeDeclined() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTheGuildpact()));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        openingHarness.assertInHand(openingHarness.getPlayer1(), "Leyline of the Guildpact");
        openingHarness.assertNotOnBattlefield(openingHarness.getPlayer1(), "Leyline of the Guildpact");
    }

    @Test
    void opponentLandsDoNotGainAdditionalManaColors() {
        harness.addToBattlefield(player1, new LeylineOfTheGuildpact());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void artifactLandsStayColorlessAndKeepTheirOriginalManaAbility() {
        harness.addToBattlefield(player1, new LeylineOfTheGuildpact());
        Permanent scene = harness.addToBattlefieldAndReturn(player1, new SceneOfTheCrime());

        assertThat(gqs.getEffectiveColors(gd, scene)).isEmpty();
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(scene.isTapped()).isTrue();
    }

    @Test
    void nonlandPermanentsLoseTheGrantedColorsWhenLeylineLeaves() {
        harness.addToBattlefield(player1, new LeylineOfTheGuildpact());
        Permanent maverick = harness.addToBattlefieldAndReturn(player1, new RubblebeltMaverick());
        assertThat(gqs.getEffectiveColors(gd, maverick)).hasSize(5);

        gd.playerBattlefields.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectiveColors(gd, maverick)).containsExactly(CardColor.GREEN);
    }

    @Test
    @CardUsed({LeylineOfTheGuildpact.class, SceneOfTheCrime.class, BloodMoon.class})
    void laterBloodMoonDoesNotLeaveAnExtraAnyColorManaAbility() {
        harness.enterBattlefieldAndReturn(player1, new LeylineOfTheGuildpact());
        Permanent scene = harness.enterBattlefieldAndReturn(player1, new SceneOfTheCrime());
        harness.enterBattlefieldAndReturn(player2, new BloodMoon());
        scene.untap();

        assertThat(gqs.intrinsicBasicLandManaColors(gd, scene)).containsExactly(ManaColor.RED);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 3, null, null))
                .isInstanceOf(RuntimeException.class);
    }
}
