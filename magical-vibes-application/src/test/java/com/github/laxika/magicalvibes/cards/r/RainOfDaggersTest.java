package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.cards.m.MoanOfTheUnhallowed;
import com.github.laxika.magicalvibes.cards.s.SkeletalGrimace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RainOfDaggers.class, AlabornTrooper.class, Forest.class, ManorGargoyle.class,
        MoanOfTheUnhallowed.class, SkeletalGrimace.class})
class RainOfDaggersTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys all creatures the target opponent controls and controller loses 2 life each")
    void destroysOpponentCreaturesAndLosesLife() {
        harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefieldAndReturn(player1, new AlabornTrooper());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Alaborn Trooper");
        harness.assertLife(player1, STARTING_LIFE - 4);
    }

    @Test
    @DisplayName("Loses no life when the target opponent controls no creatures")
    void losesNoLifeWithNoCreatures() {
        harness.addToBattlefield(player1, new AlabornTrooper());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, STARTING_LIFE);
    }

    @Test
    @DisplayName("Indestructible creatures are not destroyed and do not count toward life loss")
    void indestructibleNotDestroyedNotCounted() {
        harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        // Manor Gargoyle has defender, so its static ability makes it indestructible.
        harness.addToBattlefieldAndReturn(player2, new ManorGargoyle());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertOnBattlefield(player2, "Manor Gargoyle");
        harness.assertLife(player1, STARTING_LIFE - 2);
    }

    @Test
    @DisplayName("Regenerated creatures survive and do not count toward life loss")
    void regeneratedCreatureDoesNotCount() {
        var survivor = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        var aura = harness.addToBattlefieldAndReturn(player2, new SkeletalGrimace());
        aura.setAttachedTo(survivor.getId());
        harness.addToBattlefield(player2, new AlabornTrooper());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, player2.getId());

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard() instanceof AlabornTrooper)
                .containsExactly(survivor);
        assertThat(survivor.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Skeletal Grimace");
        harness.assertInGraveyard(player2, "Alaborn Trooper");
        harness.assertLife(player1, STARTING_LIFE - 2);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Destroyed creature tokens count toward life loss")
    void destroyedTokensCount() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MoanOfTheUnhallowed()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, 0);
        harness.assertOnBattlefield(player2, "Zombie");
        harness.addToBattlefield(player2, new AlabornTrooper());

        harness.forceActivePlayer(player1);
        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Zombie");
        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertLife(player1, STARTING_LIFE - 6);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
