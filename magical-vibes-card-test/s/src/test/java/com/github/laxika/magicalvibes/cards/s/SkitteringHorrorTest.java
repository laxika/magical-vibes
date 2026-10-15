package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BubblingMuck;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkitteringHorror.class, MetathranSoldier.class, BubblingMuck.class})
class SkitteringHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell triggers Skittering Horror's sacrifice ability")
    void castingCreatureSpellTriggersSacrifice() {
        harness.addToBattlefield(player1, new SkitteringHorror());
        harness.setHand(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Skittering Horror"));
    }

    @Test
    @DisplayName("Resolving the trigger sacrifices Skittering Horror")
    void triggerSacrificesHorror() {
        harness.addToBattlefield(player1, new SkitteringHorror());
        harness.setHand(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skittering Horror");
        harness.assertInGraveyard(player1, "Skittering Horror");
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger Skittering Horror")
    void castingNoncreatureSpellDoesNotSacrifice() {
        harness.addToBattlefield(player1, new SkitteringHorror());
        harness.setHand(player1, List.of(new BubblingMuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        harness.assertOnBattlefield(player1, "Skittering Horror");
    }

    @Test
    @DisplayName("An opponent's creature spell does not trigger Skittering Horror")
    void opponentCreatureSpellDoesNotSacrifice() {
        harness.addToBattlefield(player1, new SkitteringHorror());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MetathranSoldier()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        harness.assertOnBattlefield(player1, "Skittering Horror");
    }

    @Test
    @DisplayName("Skittering Horror does not trigger from its own casting")
    void castingHorrorDoesNotSacrificeItself() {
        harness.setHand(player1, List.of(new SkitteringHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skittering Horror");
        harness.assertNotInGraveyard(player1, "Skittering Horror");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting another Horror sacrifices the existing Horror before the new one resolves")
    void castingAnotherHorrorSacrificesOnlyExistingHorror() {
        SkitteringHorror existing = new SkitteringHorror();
        SkitteringHorror incoming = new SkitteringHorror();
        harness.addToBattlefield(player1, existing);
        harness.setHand(player1, List.of(incoming));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skittering Horror");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(existing).doesNotContain(incoming);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(incoming.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(incoming.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature entering without being cast does not trigger Skittering Horror")
    void enteringCreatureWithoutCastingDoesNotSacrifice() {
        harness.addToBattlefield(player1, new SkitteringHorror());

        harness.enterBattlefieldAndReturn(player1, new MetathranSoldier());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Skittering Horror");
        harness.assertOnBattlefield(player1, "Metathran Soldier");
        harness.assertNotInGraveyard(player1, "Skittering Horror");
    }
}
