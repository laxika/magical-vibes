package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinBerserker;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.cards.p.PsychicPaper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakeOfDestruction.class, YavimayaHollow.class, GoblinBerserker.class,
        MishrasFactory.class, PsychicPaper.class})
class WakeOfDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target land and every other land with the same name")
    void destroysTargetAndAllSameNameLands() {
        harness.addToBattlefield(player2, new YavimayaHollow());
        harness.addToBattlefield(player1, new YavimayaHollow());
        harness.addToBattlefield(player2, new GoblinBerserker());

        UUID targetId = harness.getPermanentId(player2, "Yavimaya Hollow");
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Yavimaya Hollow");
        harness.assertNotOnBattlefield(player2, "Yavimaya Hollow");
        harness.assertOnBattlefield(player2, "Goblin Berserker");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GoblinBerserker());
        harness.addToBattlefield(player2, new YavimayaHollow());
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Goblin Berserker");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    void canTargetControllersOwnLand() {
        harness.addToBattlefield(player1, new YavimayaHollow());
        harness.addToBattlefield(player2, new YavimayaHollow());
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Yavimaya Hollow"));

        harness.assertInGraveyard(player1, "Yavimaya Hollow");
        harness.assertInGraveyard(player2, "Yavimaya Hollow");
    }

    @Test
    void doesNotDestroyOtherLandsWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaHollow());
        harness.addToBattlefield(player1, new YavimayaHollow());
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castSorcery(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yavimaya Hollow");
        harness.assertInHand(player2, "Yavimaya Hollow");
        harness.assertInGraveyard(player1, "Wake of Destruction");
    }

    @Test
    void comparesCurrentNamesOfLandsRatherThanPrintedNames() {
        harness.addToBattlefield(player1, new PsychicPaper());
        Permanent renamedLand = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new MishrasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, renamedLand.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Goblin Berserker");
        harness.handleListChoice(player1, "GOBLIN");
        assertThat(gqs.getEffectiveName(gd, renamedLand)).isEqualTo("Goblin Berserker");
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, renamedLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(renamedLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherLand);
    }
}
