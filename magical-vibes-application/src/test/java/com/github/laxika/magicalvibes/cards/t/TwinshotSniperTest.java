package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinshotSniper.class, GrizzlyBears.class})
class TwinshotSniperTest extends BaseCardTest {

    @Test
    void entersAndDealsTwoDamageToAnyTargetPlayer() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.setLife(player2, 20);
        addCastingMana();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void entersAndDealsTwoDamageToAnyTargetCreature() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addCastingMana();

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void channelDealsTwoDamageToAnyTargetPlayerAndDiscardsSource() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.setLife(player2, 20);
        addChannelMana();

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Twinshot Sniper");
    }

    @Test
    void channelDealsTwoDamageToAnyTargetCreature() {
        harness.setHand(player1, List.of(new TwinshotSniper()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addChannelMana();

        harness.activateHandAbility(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Twinshot Sniper");
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addChannelMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
