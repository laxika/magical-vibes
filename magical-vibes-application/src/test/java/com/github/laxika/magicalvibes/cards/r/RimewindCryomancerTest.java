package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RimewindCryomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability with four snow permanents")
    void countersActivatedAbilityWithFourSnowPermanents() {
        addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, fumeSpitter.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Fume Spitter");
    }

    @Test
    @DisplayName("Requires four snow permanents you control")
    void requiresFourSnowPermanentsYouControl() {
        addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 3);
        addSnowPermanents(player2, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 1, null, target.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fumeSpitter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more snow permanents");
    }

    @Test
    @DisplayName("Cannot target a spell")
    void cannotTargetSpell() {
        addCreatureReady(player1, new RimewindCryomancer());
        addSnowPermanents(player1, 4);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSnowPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent snow = new Permanent(new GrizzlyBears());
            TestCards.mutableCard(snow).setSupertypes(EnumSet.of(CardSupertype.SNOW));
            gd.playerBattlefields.get(player.getId()).add(snow);
        }
    }
}
