package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LeylineOfTheGuildpact.class, Forest.class, GrizzlyBears.class})
class LeylineOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTheGuildpact()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of the Guildpact"));
    }

    @Test
    @DisplayName("Leyline makes your nonland permanents all colors")
    void makesOwnNonlandPermanentsAllColors() {
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfTheGuildpact());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveColors(gd, leyline))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, opponentBears))
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
}
