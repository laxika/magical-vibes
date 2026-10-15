package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MakindiGriffin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LastKiss.class, MakindiGriffin.class, GlorySeeker.class, Forest.class})
class LastKissTest extends BaseCardTest {

    @Test
    @DisplayName("Can target your own creature")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MakindiGriffin());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new LastKiss()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The second player gains life when they cast Last Kiss")
    void secondPlayerCasterGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MakindiGriffin());
        harness.setLife(player1, 15);
        harness.setLife(player2, 10);
        harness.setHand(player2, List.of(new LastKiss()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Deals 2 damage to a creature and gains 2 life")
    void dealsDamageAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MakindiGriffin());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new LastKiss()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Kills a 2-toughness creature and still gains 2 life")
    void killsCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new LastKiss()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LastKiss()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles without gaining life when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MakindiGriffin());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new LastKiss()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }
}
