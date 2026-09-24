package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.e.EchoingDecay;
import com.github.laxika.magicalvibes.cards.f.FangrenFirstborn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamsFromWithin.class, DarksteelGargoyle.class, EchoingDecay.class, FangrenFirstborn.class})
class ScreamsFromWithinTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -1/-1")
    void givesEnchantedCreatureMinusOneMinusOne() {
        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        attachScreamsTo(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("When enchanted creature dies, Screams returns attached to the only legal creature")
    void returnsAttachedToOnlyLegalCreature() {
        Permanent dyingCreature = addCreatureReady(player1, new FangrenFirstborn());
        Permanent targetCreature = addCreatureReady(player2, new DarksteelGargoyle());
        attachScreamsTo(player1, dyingCreature);

        killCreature(dyingCreature);

        Permanent aura = findPermanent(player1, "Screams from Within");
        assertThat(aura.getAttachedTo()).isEqualTo(targetCreature.getId());
        harness.assertNotInGraveyard(player1, "Screams from Within");
    }

    @Test
    @DisplayName("Controller chooses the creature for Screams to enchant")
    void controllerChoosesAmongLegalCreatures() {
        Permanent dyingCreature = addCreatureReady(player1, new FangrenFirstborn());
        Permanent firstTarget = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent chosenTarget = addCreatureReady(player2, new DarksteelGargoyle());
        attachScreamsTo(player1, dyingCreature);

        killCreature(dyingCreature);
        harness.handlePermanentChosen(player1, chosenTarget.getId());

        Permanent aura = findPermanent(player1, "Screams from Within");
        assertThat(aura.getAttachedTo()).isEqualTo(chosenTarget.getId());
        assertThat(aura.getAttachedTo()).isNotEqualTo(firstTarget.getId());
    }

    @Test
    @DisplayName("Return trigger fizzles when there is no legal creature")
    void remainsInGraveyardWithoutLegalCreature() {
        Permanent dyingCreature = addCreatureReady(player1, new FangrenFirstborn());
        attachScreamsTo(player1, dyingCreature);

        killCreature(dyingCreature);

        harness.assertInGraveyard(player1, "Screams from Within");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Screams from Within"));
    }

    private Permanent attachScreamsTo(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ScreamsFromWithin());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new EchoingDecay()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
