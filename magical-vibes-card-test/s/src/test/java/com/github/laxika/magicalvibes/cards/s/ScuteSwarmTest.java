package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.d.DeadlyAlliance;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScuteSwarm.class, Forest.class, AshayaSoulOfTheWild.class, IntoTheRoil.class, DeadlyAlliance.class})
class ScuteSwarmTest extends BaseCardTest {

    @Test
    void landfallCreatesInsectBelowSixLands() {
        harness.addToBattlefield(player1, new ScuteSwarm());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Insect");
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void landfallCreatesScuteSwarmCopyAtSixLands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player1, new ScuteSwarm());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(scuteSwarmTokenCount()).isEqualTo(1);

        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(scuteSwarmTokenCount()).isEqualTo(3);
    }

    @Test
    void landfallTriggersForItsOwnEntryWhenAshayaMakesItALand() {
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());

        harness.castFromHand(player1, new ScuteSwarm(), "{2}{G}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.getCard().getName()).isEqualTo("Insect"));
    }

    @Test
    void createsCopyEvenWhenSourceIsReturnedToHandInResponse() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScuteSwarm());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.playLand(player1, 0);
        harness.castInstant(player2, 0, source.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Scute Swarm");
        assertThat(scuteSwarmTokenCount()).isEqualTo(1);
    }

    @Test
    void landCountIsCheckedAtResolutionAfterAshayaLeaves() {
        Permanent ashaya = harness.addToBattlefieldAndReturn(player1, new AshayaSoulOfTheWild());
        harness.addToBattlefield(player1, new ScuteSwarm());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new DeadlyAlliance()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.playLand(player1, 0);
        harness.castInstant(player2, 0, ashaya.getId());
        resolveAllTriggers();

        assertThat(scuteSwarmTokenCount()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.getCard().getName()).isEqualTo("Insect"));
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new ScuteSwarm());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private long scuteSwarmTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Scute Swarm"))
                .count();
    }
}
