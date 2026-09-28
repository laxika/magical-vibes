package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.i.IntoTheFray;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyfireKirin.class, IntoTheFray.class, SakuraTribeScout.class,
        ArabaMothrider.class, GhostLitRedeemer.class})
class SkyfireKirinTest extends BaseCardTest {

    @Test
    @DisplayName("May gain control of a creature with the triggering Arcane spell's mana value")
    void gainsControlOfExactManaValueCreature() {
        Permanent scout = prepareArcaneCast();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(scout.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(
                harness.getPermanentId(player2, "Araba Mothrider"));

        harness.handlePermanentChosen(player1, scout.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sakura-Tribe Scout");
        harness.assertNotOnBattlefield(player2, "Sakura-Tribe Scout");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("Declining the may ability leaves the creature under its owner's control")
    void decliningLeavesCreatureAlone() {
        prepareArcaneCast();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("A Spirit spell also triggers the may ability")
    void spiritSpellTriggersAbility() {
        harness.addToBattlefield(player1, new SkyfireKirin());
        Permanent scout = addCreatureReady(player2, new SakuraTribeScout());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GhostLitRedeemer(), "{W}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(scout.getId());

        harness.handlePermanentChosen(player1, scout.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sakura-Tribe Scout");
        harness.assertNotOnBattlefield(player2, "Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("A non-Spirit, non-Arcane spell does not trigger")
    void nonMatchingSpellDoesNotTrigger() {
        Permanent scout = addCreatureReady(player2, new SakuraTribeScout());
        harness.addToBattlefield(player1, new SkyfireKirin());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new ArabaMothrider(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scout);
    }

    @Test
    @DisplayName("A Spirit spell cast by an opponent does not trigger")
    void opponentSpiritSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkyfireKirin());
        Permanent scout = addCreatureReady(player2, new SakuraTribeScout());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GhostLitRedeemer(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scout);
    }

    private Permanent prepareArcaneCast() {
        harness.addToBattlefield(player1, new SkyfireKirin());
        Permanent scout = addCreatureReady(player2, new SakuraTribeScout());
        addCreatureReady(player2, new ArabaMothrider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, scout.getId());
        harness.passBothPriorities();
        return scout;
    }
}
