package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SacellumGodspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap adds {G} for each power-5-or-greater creature card in hand")
    void tapAddsGreenPerBigCreature() {
        harness.addToBattlefield(player1, new SacellumGodspeaker());
        Permanent godspeaker = gd.playerBattlefields.get(player1.getId()).getFirst();
        godspeaker.setSummoningSick(false);

        // Two 8/8 creatures qualify (power >= 5).
        harness.setHand(player1, List.of(new AvatarOfMight(), new AvatarOfMight()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Small creatures and non-creatures in hand are not counted")
    void ignoresSmallCreaturesAndNoncreatures() {
        harness.addToBattlefield(player1, new SacellumGodspeaker());
        Permanent godspeaker = gd.playerBattlefields.get(player1.getId()).getFirst();
        godspeaker.setSummoningSick(false);

        // One qualifying 8/8, one 2/2 (power < 5), one instant.
        harness.setHand(player1, List.of(new AvatarOfMight(), new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Produces no mana with no qualifying creatures in hand")
    void producesNoManaWithoutBigCreatures() {
        harness.addToBattlefield(player1, new SacellumGodspeaker());
        Permanent godspeaker = gd.playerBattlefields.get(player1.getId()).getFirst();
        godspeaker.setSummoningSick(false);

        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
