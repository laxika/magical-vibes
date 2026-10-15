package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.cards.s.SaguMauler;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheDragonspeaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcLightning.class, AlpineGrizzly.class, ForceAway.class, SaguMauler.class,
        SarkhanTheDragonspeaker.class})
class ArcLightningTest extends BaseCardTest {

    @Test
    void dealsAllDamageToOneTarget() {
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 3));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void dividesDamageAmongTwoTargets() {
        Permanent grizzly = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(grizzly.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertLife(player2, 19);
    }

    @Test
    void dividesDamageAmongThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0,
                Map.of(first.getId(), 1, second.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    void assignmentsMustSumToThree() {
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void everyTargetMustReceivePositiveDamage() {
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(player2.getId(), 3, player1.getId(), 0))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseNoTargets() {
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDamageControllerAndTheirCreature() {
        Permanent grizzly = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(grizzly.getId(), 2, player1.getId(), 1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void dividedDamageRemovesPlaneswalkerLoyalty() {
        Permanent sarkhan = harness.enterBattlefieldAndReturn(player2, new SarkhanTheDragonspeaker());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(sarkhan.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Sarkhan, the Dragonspeaker");
        harness.assertLife(player2, 19);
    }

    @Test
    void cannotTargetOpponentsHexproofCreature() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player2, new SaguMauler());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(mauler.getId(), 2, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnHexproofCreature() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new SaguMauler());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(mauler.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(mauler.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Sagu Mauler");
        harness.assertLife(player2, 19);
    }

    @Test
    void damageAssignedToRemovedTargetIsNotRedistributed() {
        Permanent grizzly = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new ArcLightning(), new ForceAway()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, Map.of(grizzly.getId(), 2, player2.getId(), 1));
        harness.castAndResolveInstant(player1, 0, grizzly.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertInHand(player2, "Alpine Grizzly");
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Arc Lightning");
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetLeavesBattlefield() {
        Permanent grizzly = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new ArcLightning(), new ForceAway()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, Map.of(grizzly.getId(), 3));
        harness.castAndResolveInstant(player1, 0, grizzly.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertInHand(player2, "Alpine Grizzly");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Arc Lightning");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
