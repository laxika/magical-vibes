package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrevasRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB cost returns a non-Lair land and keeps the source")
    void acceptsEtbCostByReturningNonLairLand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        TrevasRuins ruins = new TrevasRuins();
        playAndResolveEtb(ruins);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == ruins)
                .doesNotContain(plains);
        assertThat(gd.playerHands.get(player1.getId())).contains(plains.getCard());
    }

    @Test
    @DisplayName("The source is sacrificed when only a Lair land is available")
    void sacrificesWhenOnlyLairLandIsAvailable() {
        harness.addToBattlefield(player1, new TrevasRuins());
        playAndResolveEtb(new TrevasRuins());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TrevasRuins))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof TrevasRuins))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the ETB cost sacrifices the source")
    void decliningEtbCostSacrificesSource() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        TrevasRuins ruins = new TrevasRuins();
        playAndResolveEtb(ruins);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == ruins)
                .anyMatch(permanent -> permanent == plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ruins);
    }

    @Test
    @DisplayName("The mana ability offers green, white, and blue")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new TrevasRuins());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE", "BLUE");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps the source")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent ruins = harness.addToBattlefieldAndReturn(player1, new TrevasRuins());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(ruins.isTapped()).isTrue();
    }

    private void playAndResolveEtb(TrevasRuins ruins) {
        harness.setHand(player1, List.of(ruins));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
