package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrilledMystic.class, GrizzlyBears.class, LightningBolt.class, SauroformHybrid.class})
class FrilledMysticTest extends BaseCardTest {

    @Test
    @DisplayName("ETB cannot target an activated ability")
    void cannotCounterActivatedAbility() {
        var hybrid = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player1, List.of(new FrilledMystic()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Frilled Mystic");
        harness.passBothPriorities();

        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows countering an opponent's creature spell on their turn")
    void countersOpponentsCreatureOnTheirTurn() {
        SauroformHybrid hybrid = new SauroformHybrid();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(hybrid));
        harness.setHand(player1, List.of(new FrilledMystic()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);
        gs.passPriority(gd, player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(hybrid.getId());
        harness.handlePermanentChosen(player1, hybrid.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Sauroform Hybrid");
        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertOnBattlefield(player1, "Frilled Mystic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB may counter a target spell")
    void etbCountersTargetSpell() {
        LightningBolt bolt = new LightningBolt();
        FrilledMystic mystic = new FrilledMystic();
        harness.setHand(player1, List.of(bolt, mystic));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bolt.getId());

        harness.handlePermanentChosen(player1, bolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertOnBattlefield(player1, "Frilled Mystic");
    }

    @Test
    @DisplayName("Declining the ETB may lets the target spell resolve")
    void decliningMayLetsSpellResolve() {
        GrizzlyBears bears = new GrizzlyBears();
        FrilledMystic mystic = new FrilledMystic();
        harness.setHand(player1, List.of(bears, mystic));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Frilled Mystic");
    }

    @Test
    @DisplayName("ETB does not trigger when no spell is on the stack")
    void noTriggerWithoutSpellOnStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FrilledMystic()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Frilled Mystic");
    }
}
