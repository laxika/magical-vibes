package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArmoredGriffin;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkOffering.class, ArmoredGriffin.class, DakmorBat.class, Plains.class})
class DarkOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature and gains 3 life")
    void destroysNonblackCreatureAndGainsLife() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new ArmoredGriffin());
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, griffin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Armored Griffin");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new ArmoredGriffin()); // legal target so the spell is playable
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new DakmorBat());
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Can target a nonblack creature the controller owns")
    void canTargetOwnNonblackCreature() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new ArmoredGriffin());
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, griffin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Armored Griffin");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A creature with a regeneration shield is regenerated instead of destroyed")
    void regenerationShieldSavesCreature() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new ArmoredGriffin());
        griffin.setRegenerationShield(1);
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, griffin.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Armored Griffin");
        harness.assertNotInGraveyard(player2, "Armored Griffin");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Fizzles without gaining life if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new ArmoredGriffin());
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, griffin.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Dark Offering");
    }
}
