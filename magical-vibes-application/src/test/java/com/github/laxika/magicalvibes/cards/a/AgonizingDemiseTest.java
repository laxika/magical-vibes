package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NightscapeApprentice;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgonizingDemise.class, YavimayaBarbarian.class, NightscapeApprentice.class, Forest.class})
class AgonizingDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature without kicker")
    void destroysCreatureWithoutKicker() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        cast(barbarian, false);

        harness.assertNotOnBattlefield(player2, "Yavimaya Barbarian");
        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroys the creature and deals damage equal to its power when kicked")
    void destroysCreatureAndDamagesItsControllerWhenKicked() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        cast(barbarian, true);

        harness.assertNotOnBattlefield(player2, "Yavimaya Barbarian");
        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The destruction cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player2, new YavimayaBarbarian());
        barbarian.setRegenerationShield(1);
        cast(barbarian, false);

        harness.assertNotOnBattlefield(player2, "Yavimaya Barbarian");
        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new NightscapeApprentice());
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        if (kicked) {
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
        }

        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }
}
