package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatDivideGuide.class, Forest.class, HadaFreeblade.class, GrizzlyBears.class})
class GreatDivideGuideTest extends BaseCardTest {

    @Test
    void landsAndAlliesYouControlGainAnyColorManaAbility() {
        harness.addToBattlefield(player1, new GreatDivideGuide());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player1, new HadaFreeblade());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 2, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 3, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void opponentLandsDoNotGainTheAbility() {
        harness.addToBattlefield(player1, new GreatDivideGuide());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void guideCanTapForManaItselfWithoutUsingTheStack() {
        var guide = addCreatureReady(player1, new GreatDivideGuide());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(guide.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void summoningSickGuideCannotTapForMana() {
        harness.addToBattlefield(player1, new GreatDivideGuide());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void opponentAlliesDoNotGainTheAbility() {
        harness.addToBattlefield(player1, new GreatDivideGuide());
        addCreatureReady(player2, new HadaFreeblade());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void grantedAbilityDisappearsWhenGuideLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new GreatDivideGuide());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLACK");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
