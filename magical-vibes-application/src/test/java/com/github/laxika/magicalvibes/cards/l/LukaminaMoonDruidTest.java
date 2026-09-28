package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LukaminaMoonDruid.class, Forest.class, GrizzlyBears.class, Shock.class, DoomBlade.class})
class LukaminaMoonDruidTest extends BaseCardTest {

    @Test
    void castLukaminaSeeksALandWithABasicLandType() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new Shock(), forest));
        harness.setHand(player1, List.of(new LukaminaMoonDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(forest.getId()));
    }

    @Test
    void specializesIntoHawkForm() {
        assertThat(specialize(0).getCard().getName()).isEqualTo("Lukamina, Hawk Form");
    }

    @Test
    void specializesIntoCrocodileForm() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(specialize(1, target).getCard().getName()).isEqualTo("Lukamina, Crocodile Form");
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void specializesIntoScorpionForm() {
        assertThat(specialize(2).getCard().getName()).isEqualTo("Lukamina, Scorpion Form");
    }

    @Test
    void specializesIntoWolfForm() {
        assertThat(specialize(3).getCard().getName()).isEqualTo("Lukamina, Wolf Form");
    }

    @Test
    void specializesIntoBearForm() {
        assertThat(specialize(4).getCard().getName()).isEqualTo("Lukamina, Bear Form");
    }

    @Test
    void wolfFormCreatesAWolfWhenItSpecializes() {
        Permanent wolf = specialize(3);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
        assertThat(wolf.getCard().getName()).isEqualTo("Lukamina, Wolf Form");
    }

    @Test
    void specializedLukaminaReturnsUnspecializedWhenItDies() {
        Permanent hawk = specialize(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, hawk.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Lukamina, Moon Druid");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(hawk.getCard().getId()));
    }

    private Permanent specialize(int colorAbilityIndex) {
        return specialize(colorAbilityIndex, null);
    }

    private Permanent specialize(int colorAbilityIndex, Permanent target) {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent lukamina = harness.addToBattlefieldAndReturn(player1, new LukaminaMoonDruid());
        lukamina.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lukamina);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, permanentIndex, colorAbilityIndex, null, null);
        harness.passBothPriorities();
        if (target != null) {
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        }
        return lukamina;
    }
}
