package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeteorBlast.class, GrizzlyBears.class, Plains.class, SakuraTribeElder.class})
class MeteorBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each of exactly X targets")
    void dealsFourDamageToEachTarget() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 2, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals the full 4 damage to creature and player targets")
    void dealsFullDamageToCreatureAndPlayer() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 2, List.of(bear.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Requires exactly X targets")
    void requiresExactlyXTargets() {
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a land as an any-target")
    void rejectsLandTarget() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast with X zero and no targets")
    void canCastWithZeroTargets() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Meteor Blast");
    }

    @Test
    @DisplayName("Cannot choose the same target twice")
    void rejectsDuplicateTargets() {
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more targets than X")
    void rejectsTooManyTargets() {
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still deals 4 damage to a legal target when another target leaves")
    void resolvesWithOneRemainingTarget() {
        Permanent elder = harness.addToBattlefieldAndReturn(player2, new SakuraTribeElder());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 2, List.of(elder.getId(), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(elder);
        harness.setExile(player2, List.of(elder.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Meteor Blast");
    }

    @Test
    @DisplayName("Can choose more than 100 distinct targets when X is greater than 100")
    void canChooseMoreThanOneHundredTargets() {
        List<UUID> targets = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new SakuraTribeElder()).getId())
                .toList();
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 104);

        harness.castSorcery(player1, 0, 101, targets);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(101);
        harness.assertInGraveyard(player1, "Meteor Blast");
    }
}
