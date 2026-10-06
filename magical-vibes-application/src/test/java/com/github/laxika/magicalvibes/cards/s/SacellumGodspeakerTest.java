package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mosstodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SacellumGodspeaker.class, AvatarOfMight.class, GrizzlyBears.class, Shock.class, Mosstodon.class})
class SacellumGodspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap adds {G} for each power-5-or-greater creature card in hand")
    void tapAddsGreenPerBigCreature() {
        addCreatureReady(player1, new SacellumGodspeaker());

        // Two 8/8 creatures qualify (power >= 5).
        harness.setHand(player1, List.of(new AvatarOfMight(), new AvatarOfMight()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Small creatures and non-creatures in hand are not counted")
    void ignoresSmallCreaturesAndNoncreatures() {
        addCreatureReady(player1, new SacellumGodspeaker());

        // One qualifying 8/8, one 2/2 (power < 5), one instant.
        harness.setHand(player1, List.of(new AvatarOfMight(), new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Produces no mana with no qualifying creatures in hand")
    void producesNoManaWithoutBigCreatures() {
        addCreatureReady(player1, new SacellumGodspeaker());

        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
    @Test
    void powerExactlyFiveQualifiesAndCardsRemainInHand() {
        Permanent godspeaker = addCreatureReady(player1, new SacellumGodspeaker());
        Mosstodon creature = new Mosstodon();
        harness.setHand(player1, List.of(creature));
        harness.setHand(player2, List.of(new AvatarOfMight()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(godspeaker.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerMustBeAllowedToChooseHowManyCardsToReveal() {
        addCreatureReady(player1, new SacellumGodspeaker());
        harness.setHand(player1, List.of(new AvatarOfMight(), new Mosstodon()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent godspeaker = harness.addToBattlefieldAndReturn(player1, new SacellumGodspeaker());
        godspeaker.setSummoningSick(true);
        harness.setHand(player1, List.of(new Mosstodon()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(godspeaker.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void cannotActivateWhileAlreadyTapped() {
        Permanent godspeaker = addCreatureReady(player1, new SacellumGodspeaker());
        godspeaker.tap();
        harness.setHand(player1, List.of(new Mosstodon()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
