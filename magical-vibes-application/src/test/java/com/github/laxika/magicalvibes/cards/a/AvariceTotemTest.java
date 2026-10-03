package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoThinAir;
import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvariceTotem.class, Arachnoid.class, Forest.class, IntoThinAir.class, VedalkenOrrery.class})
class AvariceTotemTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Exchanges control of itself and the target nonland permanent")
    void exchangesControl() {
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Avarice Totem");
        harness.assertNotOnBattlefield(player1, "Avarice Totem");
        harness.assertOnBattlefield(player1, "Arachnoid");
        harness.assertNotOnBattlefield(player2, "Arachnoid");
    }

    @Test
    @DisplayName("Can target a nonland permanent controlled by the same player")
    void canTargetOwnNonlandPermanent() {
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avarice Totem");
        harness.assertOnBattlefield(player1, "Arachnoid");
        harness.assertNotOnBattlefield(player2, "Avarice Totem");
        harness.assertNotOnBattlefield(player2, "Arachnoid");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Targeting itself does not change control")
    void canTargetItself() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        addActivationMana();

        harness.activateAbility(player1, 0, null, totem.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avarice Totem");
        harness.assertNotOnBattlefield(player2, "Avarice Totem");
    }

    @Test
    @DisplayName("Two activations can recover the Totem by exchanging an owned permanent")
    void exchangesUsingCurrentControllers() {
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AvariceTotem());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, own.getId());
        harness.activateAbility(player1, 0, null, opposing.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arachnoid");
        harness.assertOnBattlefield(player2, "Arachnoid");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2).allMatch(p -> p.getCard().getName().equals("Avarice Totem"));
        harness.assertNotOnBattlefield(player2, "Avarice Totem");
    }

    @Test
    @DisplayName("No exchange occurs when the source leaves before resolution")
    void sourceLeavesBeforeResolution() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new IntoThinAir()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, totem.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Avarice Totem");
        harness.assertOnBattlefield(player2, "Arachnoid");
        harness.assertNotOnBattlefield(player1, "Arachnoid");
        harness.assertNotOnBattlefield(player2, "Avarice Totem");
    }

    @Test
    @DisplayName("No exchange occurs when the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new IntoThinAir()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Arachnoid");
        harness.assertOnBattlefield(player1, "Avarice Totem");
        harness.assertNotOnBattlefield(player2, "Avarice Totem");
        harness.assertNotOnBattlefield(player1, "Arachnoid");
    }

    @Test
    @DisplayName("A returned Totem cannot be exchanged by its previous incarnation's ability")
    void returnedSourceIsANewPermanent() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new IntoThinAir()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, totem.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Avarice Totem");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avarice Totem");
        harness.assertNotOnBattlefield(player2, "Avarice Totem");
        harness.assertOnBattlefield(player2, "Arachnoid");
        harness.assertNotOnBattlefield(player1, "Arachnoid");
    }
}
