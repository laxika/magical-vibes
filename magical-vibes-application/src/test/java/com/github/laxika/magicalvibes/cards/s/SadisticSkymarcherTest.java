package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionZealot;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SadisticSkymarcher.class, DuskLegionZealot.class, SailorOfMeans.class})
class SadisticSkymarcherTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Vampire in hand it costs {2}{B} plus the additional {1}")
    void requiresExtraOneWithoutVampire() {
        harness.setHand(player1, List.of(new SadisticSkymarcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {1} can be paid with mana when no Vampire is revealed")
    void payTheOneWithMana() {
        SadisticSkymarcher skymarcher = new SadisticSkymarcher();
        harness.setHand(player1, List.of(skymarcher));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // {2} + {1}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sadistic Skymarcher");
    }

    @Test
    @DisplayName("Revealing a Vampire card from hand lets it be cast for just {2}{B}")
    void revealVampireAvoidsTheOne() {
        SadisticSkymarcher skymarcher = new SadisticSkymarcher();
        DuskLegionZealot vampireInHand = new DuskLegionZealot();
        harness.setHand(player1, List.of(skymarcher, vampireInHand));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sadistic Skymarcher");
        harness.assertInHand(player1, "Dusk Legion Zealot");
    }

    @Test
    void vampireIsPubliclyRevealedBeforeSpellResolves() {
        harness.setHand(player1, List.of(new SadisticSkymarcher(), new DuskLegionZealot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals")
                && entry.plainText().contains("Dusk Legion Zealot"));
        harness.assertInHand(player1, "Dusk Legion Zealot");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sadistic Skymarcher");
    }

    @Test
    void nonVampireInHandDoesNotWaiveAdditionalCost() {
        harness.setHand(player1, List.of(new SadisticSkymarcher(), new SailorOfMeans()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsVampireDoesNotWaiveAdditionalCost() {
        harness.setHand(player1, List.of(new SadisticSkymarcher()));
        harness.setHand(player2, List.of(new DuskLegionZealot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void battlefieldVampireDoesNotWaiveAdditionalCost() {
        harness.setHand(player1, List.of(new SadisticSkymarcher()));
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revealingVampireDoesNotReplaceBlackManaRequirement() {
        harness.setHand(player1, List.of(new SadisticSkymarcher(), new DuskLegionZealot()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new SadisticSkymarcher());
        addCreatureReady(player2, new SailorOfMeans());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unblockedCombatDamageGainsLife() {
        addCreatureReady(player1, new SadisticSkymarcher());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }
}
