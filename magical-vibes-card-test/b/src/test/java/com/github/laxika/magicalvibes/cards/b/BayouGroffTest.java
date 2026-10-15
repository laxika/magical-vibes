package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoreclawTerrorOfQalSisma;
import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BayouGroff.class, SpinedKarok.class, GoreclawTerrorOfQalSisma.class})
class BayouGroffTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost and enters the battlefield")
    void sacrificesCreatureAsAdditionalCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());

        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spined Karok");
        harness.assertInGraveyard(player1, "Spined Karok");
        harness.assertOnBattlefield(player1, "Bayou Groff");
    }

    @Test
    @DisplayName("Pays {3} instead of sacrificing and enters the battlefield")
    void paysManaInsteadOfSacrificing() {
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorceryWithSacrifice(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bayou Groff");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without a creature or enough mana for the alternate cost")
    void cannotCastWithoutCreatureOrMana() {
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Sacrifice is paid before anyone can respond")
    void sacrificeIsPaidBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Spined Karok");
        harness.assertInGraveyard(player1, "Spined Karok");
        harness.assertNotOnBattlefield(player1, "Bayou Groff");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bayou Groff");
    }

    @Test
    @DisplayName("May pay mana even when a creature is available")
    void payingManaPreservesAvailableCreature() {
        harness.addToBattlefield(player1, new SpinedKarok());
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorceryWithSacrifice(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spined Karok");
        harness.assertOnBattlefield(player1, "Bayou Groff");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature for the additional cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Spined Karok");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @CardUsed({BayouGroff.class, GoreclawTerrorOfQalSisma.class})
    @DisplayName("Cost reduction applies to the combined mana and additional cost")
    void costReductionAppliesToAdditionalManaCost() {
        harness.addToBattlefield(player1, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new BayouGroff()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorceryWithSacrifice(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bayou Groff");
        harness.assertOnBattlefield(player1, "Goreclaw, Terror of Qal Sisma");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
