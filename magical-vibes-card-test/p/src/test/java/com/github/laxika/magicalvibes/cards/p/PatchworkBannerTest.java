package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatchworkBanner.class, GrizzlyBears.class})
class PatchworkBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type boosts matching creatures you control")
    void choosingCreatureTypeBoostsMatchingCreaturesYouControl() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new PatchworkBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BEAR");

        Permanent banner = findPermanent(player1, "Patchwork Banner");
        assertThat(banner.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping Patchwork Banner adds one mana of the chosen color")
    void tappingAddsAnyColorMana() {
        harness.addToBattlefield(player1, new PatchworkBanner());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonmatchingCreaturesDoNotReceiveTheBonus() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new PatchworkBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FROG");

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    void matchingCreaturesEnteringLaterReceiveTheBonus() {
        harness.setHand(player1, java.util.List.of(new PatchworkBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    void multipleBannersHaveIndependentChoicesAndCumulativeBonuses() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(
                new PatchworkBanner(), new PatchworkBanner(), new PatchworkBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        for (String type : java.util.List.of("BEAR", "FROG", "BEAR")) {
            harness.castArtifact(player1, 0);
            harness.passBothPriorities();
            harness.handleListChoice(player1, type);
        }

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    void tappingAddsEachOtherManaColor(ManaColor color) {
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new PatchworkBanner());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(banner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
