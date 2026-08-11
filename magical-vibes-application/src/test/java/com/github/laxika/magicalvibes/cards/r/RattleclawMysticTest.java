package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RattleclawMysticTest extends BaseCardTest {

    @Test
    void tappingPromptsForGreenBlueOrRed() {
        Permanent mystic = addReadyMystic();

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "BLUE", "RED");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mystic.isTapped()).isTrue();
    }

    @Test
    void turningFaceUpAddsOneManaOfEachColor() {
        harness.setHand(player1, List.of(new RattleclawMystic()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Rattleclaw Mystic");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mystic));
        harness.passBothPriorities();

        assertThat(mystic.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyMystic() {
        Permanent mystic = new Permanent(new RattleclawMystic());
        mystic.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mystic);
        return mystic;
    }
}
