package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HeadlessHorseman;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaDrain.class, HeadlessHorseman.class, HolyDay.class, WalkingBallista.class})
class ManaDrainTest extends BaseCardTest {

    private Card counterSpell(Card spell, String manaCost) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, spell, manaCost);

        harness.setHand(player2, List.of(new ManaDrain()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        return spell;
    }

    @Test
    @DisplayName("Counters a creature spell and adds colorless mana equal to its mana value at the next main phase")
    void countersAndAddsManaAtNextMainPhase() {
        Card horseman = counterSpell(new HeadlessHorseman(), "{2}{B}");

        harness.assertNotOnBattlefield(player1, horseman.getName());
        harness.assertInGraveyard(player1, horseman.getName());
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters a noncreature spell and still uses its mana value for the delayed mana")
    void countersNoncreatureSpell() {
        Card holyDay = counterSpell(new HolyDay(), "{W}");

        harness.assertNotOnBattlefield(player1, holyDay.getName());
        harness.assertInGraveyard(player1, holyDay.getName());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana Drain on your turn adds mana in your next postcombat main phase only once")
    void addsManaInSameTurnsPostcombatMainOnlyOnce() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card holyDay = new HolyDay();
        harness.castFromHand(player2, holyDay, "{W}");
        harness.setHand(player1, List.of(new ManaDrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, holyDay.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A Mana Drain whose target leaves the stack does not create delayed mana")
    void missingTargetDoesNotCreateDelayedMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card horseman = new HeadlessHorseman();
        harness.castFromHand(player1, horseman, "{2}{B}");
        harness.setHand(player2, List.of(new ManaDrain(), new ManaDrain()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, horseman.getId());
        harness.castAndResolveInstant(player2, 0, horseman.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, horseman.getName());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each X in the target spell's mana cost contributes its chosen value")
    void countsBothXSymbolsInManaValue() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card ballista = new WalkingBallista();
        harness.setHand(player1, List.of(ballista));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0, 3);
        harness.setHand(player2, List.of(new ManaDrain()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ballista.getId());

        harness.assertInGraveyard(player1, ballista.getName());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(6);
    }
}
