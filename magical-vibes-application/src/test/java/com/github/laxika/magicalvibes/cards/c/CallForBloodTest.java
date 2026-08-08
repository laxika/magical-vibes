package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallForBloodTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new CallForBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Sacrificing a 2-power creature gives the target -2/-2")
    void sacrificeTwoPowerCreature() {
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        Permanent target = new Permanent(new AirElemental()); // 4/4
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Only the targeted creature is weakened")
    void onlyTargetIsWeakened() {
        Permanent sacrifice = new Permanent(new LlanowarElves()); // 1/1
        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        Permanent bystander = new Permanent(new AirElemental()); // 4/4
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.playerBattlefields.get(player2.getId()).add(bystander);

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(bystander.getPowerModifier()).isZero();
        assertThat(bystander.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Toughness dropped to 0 destroys the target")
    void lethalDebuffDestroysTarget() {
        Permanent sacrifice = new Permanent(new AirElemental()); // 4/4
        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificed creature's power includes +1/+1 counters")
    void sacrificedPowerIncludesCounters() {
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // 5/5
        Permanent target = new Permanent(new AirElemental()); // 4/4
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        prepare();
        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        prepare();
        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
