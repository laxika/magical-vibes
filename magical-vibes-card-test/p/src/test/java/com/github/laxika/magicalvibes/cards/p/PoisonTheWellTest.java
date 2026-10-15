package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AshenmoorGouger;
import com.github.laxika.magicalvibes.cards.g.GlacialChasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoisonTheWell.class, Forest.class, AshenmoorGouger.class, GlacialChasm.class})
class PoisonTheWellTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and deals 2 damage to its controller")
    void destroysLandAndDealsDamage() {
        UUID land = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new PoisonTheWell()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, List.of(land));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        UUID creature = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger()).getId();

        harness.setHand(player1, List.of(new PoisonTheWell()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Targeting your own land damages you rather than your opponent")
    void damagesControllerOfOwnLand() {
        UUID land = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PoisonTheWell()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(land));

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An absent land target prevents both destruction and damage")
    void absentTargetDoesNotDealDamage() {
        UUID land = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PoisonTheWell()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, List.of(land));

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Poison the Well");
    }

    @Test
    @DisplayName("Glacial Chasm is destroyed before damage so its prevention no longer applies")
    void destroysDamagePreventionLandBeforeDealingDamage() {
        UUID land = harness.addToBattlefieldAndReturn(player2, new GlacialChasm()).getId();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PoisonTheWell()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(land));

        harness.assertNotOnBattlefield(player2, "Glacial Chasm");
        harness.assertInGraveyard(player2, "Glacial Chasm");
        harness.assertLife(player2, 18);
    }
}
