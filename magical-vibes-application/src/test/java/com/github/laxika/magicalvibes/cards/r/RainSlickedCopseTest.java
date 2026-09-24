package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RainSlickedCopse.class, GrizzlyBears.class})
class RainSlickedCopseTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new RainSlickedCopse()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rain-Slicked Copse").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingForGreenProducesMana() {
        addReadyCopse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingForBlueProducesMana() {
        addReadyCopse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RainSlickedCopse()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rain-Slicked Copse");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyCopse() {
        Permanent permanent = new Permanent(new RainSlickedCopse());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RainSlickedCopse()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds one chosen green or blue mana")
    void addsChosenManaColor() {
        harness.addToBattlefield(player1, new RainSlickedCopse());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
