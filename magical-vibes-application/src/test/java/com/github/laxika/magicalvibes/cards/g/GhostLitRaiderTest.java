package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BounteousKirin;
import com.github.laxika.magicalvibes.cards.m.MatsuTribeBirdstalker;
import com.github.laxika.magicalvibes.cards.r.RazorjawOni;
import com.github.laxika.magicalvibes.cards.r.RoninCavekeeper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostLitRaider.class, MatsuTribeBirdstalker.class, BounteousKirin.class,
        RoninCavekeeper.class, RazorjawOni.class})
class GhostLitRaiderTest extends BaseCardTest {

    @Test
    void battlefieldAbilityDealsTwoDamageToTargetCreature() {
        Permanent raider = addCreatureReady(player1, new GhostLitRaider());
        Permanent target = addCreatureReady(player2, new MatsuTribeBirdstalker());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Matsu-Tribe Birdstalker");
        assertThat(raider.isTapped()).isTrue();
    }

    @Test
    void battlefieldAbilityDealsExactlyTwoDamage() {
        addCreatureReady(player1, new GhostLitRaider());
        Permanent target = addCreatureReady(player2, new RoninCavekeeper());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void channelDealsFourDamageToTargetCreatureAndDiscardsSource() {
        harness.setHand(player1, List.of(new GhostLitRaider()));
        Permanent target = addCreatureReady(player2, new BounteousKirin());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost-Lit Raider");
        harness.assertInGraveyard(player2, "Bounteous Kirin");
    }

    @Test
    void channelDealsExactlyFourDamage() {
        harness.setHand(player1, List.of(new GhostLitRaider()));
        Permanent target = addCreatureReady(player2, new RazorjawOni());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void damageAbilitiesCannotTargetPlayers() {
        addCreatureReady(player1, new GhostLitRaider());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new GhostLitRaider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
