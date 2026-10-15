package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirNomadLegacy.class, GrizzlyBears.class, SuntailHawk.class, Opalescence.class, Levitation.class})
class AirNomadLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Clue token when it enters")
    void createsClueOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new AirNomadLegacy());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Boosts flying creatures you control")
    void boostsOwnFlyingCreatures() {
        harness.addToBattlefield(player1, new AirNomadLegacy());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingHawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingHawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void clueCanBeSacrificedToDraw() {
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.enterBattlefieldAndReturn(player1, new AirNomadLegacy());
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Multiple copies stack their bonuses on flying creatures")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new AirNomadLegacy());
        harness.addToBattlefield(player1, new AirNomadLegacy());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts itself when it becomes a creature with flying")
    void boostsItselfWhenAnimatedWithFlying() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new Levitation());
        Permanent legacy = harness.addToBattlefieldAndReturn(player1, new AirNomadLegacy());

        assertThat(gqs.getEffectivePower(gd, legacy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, legacy)).isEqualTo(3);
    }
}
