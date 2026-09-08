package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BirdMaiden;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrookclawElder.class, BirdMaiden.class, FugitiveWizard.class, GrizzlyBears.class})
class CrookclawElderTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two Birds draws a card")
    void tappingTwoBirdsDrawsCard() {
        Permanent elder = addReady(player1, new CrookclawElder());
        Permanent bird1 = addReady(player1, new BirdMaiden());
        Permanent bird2 = addReady(player1, new BirdMaiden());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(player1, elder), 0, null, null);
        harness.handlePermanentChosen(player1, bird1.getId());
        harness.handlePermanentChosen(player1, bird2.getId());
        harness.passBothPriorities();

        assertThat(bird1.isTapped()).isTrue();
        assertThat(bird2.isTapped()).isTrue();
        assertThat(elder.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Tapping two Wizards gives a target creature flying until end of turn")
    void tappingTwoWizardsGivesTargetFlying() {
        Permanent elder = addReady(player1, new CrookclawElder());
        Permanent wizard1 = addReady(player1, new FugitiveWizard());
        Permanent wizard2 = addReady(player1, new FugitiveWizard());
        Permanent target = addReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, elder), 1, null, target.getId());
        harness.handlePermanentChosen(player1, wizard1.getId());
        harness.handlePermanentChosen(player1, wizard2.getId());
        harness.passBothPriorities();

        assertThat(wizard1.isTapped()).isTrue();
        assertThat(wizard2.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Each ability requires two matching untapped creatures")
    void abilitiesRequireTwoMatchingCreatures() {
        Permanent elder = addReady(player1, new CrookclawElder());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, elder), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        addReady(player1, new BirdMaiden());
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, elder), 1, null, elder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        return addCreatureReady(player, card);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
