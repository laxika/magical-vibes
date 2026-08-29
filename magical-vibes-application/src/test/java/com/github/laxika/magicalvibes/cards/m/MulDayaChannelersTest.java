package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MulDayaChannelersTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 while a creature card is on top of its library")
    void getsBoostForCreatureTopCard() {
        Permanent channelers = addCreatureReady(player1, new MulDayaChannelers());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, channelers)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, channelers)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not get +3/+3 while a noncreature card is on top of its library")
    void doesNotGetBoostForNonCreatureTopCard() {
        Permanent channelers = addCreatureReady(player1, new MulDayaChannelers());
        harness.setLibrary(player1, List.of(new Shock()));

        assertThat(gqs.getEffectivePower(gd, channelers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, channelers)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains the mana ability while a land card is on top of its library")
    void gainsManaAbilityForLandTopCard() {
        Permanent channelers = addCreatureReady(player1, new MulDayaChannelers());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(channelers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not have the mana ability while a nonland card is on top of its library")
    void doesNotHaveManaAbilityForNonlandTopCard() {
        addCreatureReady(player1, new MulDayaChannelers());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The conditions change as the top card changes")
    void conditionsChangeWithTopCard() {
        Permanent channelers = addCreatureReady(player1, new MulDayaChannelers());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, channelers)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, channelers)).isEqualTo(5);

        gd.playerDecks.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, channelers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, channelers)).isEqualTo(2);
        harness.activateAbility(player1, 0, null, null);
    }
}
