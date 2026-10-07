package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlchemistsVial;
import com.github.laxika.magicalvibes.cards.c.CelestialFlare;
import com.github.laxika.magicalvibes.cards.c.ChargingGriffin;
import com.github.laxika.magicalvibes.cards.k.KytheonsTactics;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwiftReckoning.class, ChargingGriffin.class, CelestialFlare.class, KytheonsTactics.class, AlchemistsVial.class})
class SwiftReckoningTest extends BaseCardTest {

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent bear = harness.addToBattlefieldAndReturn(owner, new ChargingGriffin());
        bear.tap();
        return bear;
    }

    @Test
    @DisplayName("Destroys the targeted tapped creature")
    void destroysTappedCreature() {
        Permanent bear = addTappedCreature(player2);
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, bear.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Charging Griffin");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        addTappedCreature(player1);
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new ChargingGriffin());

        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, untapped.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spell mastery lets it be cast at instant speed")
    void spellMasteryGrantsFlashTiming() {
        Permanent bear = addTappedCreature(player2);
        harness.setGraveyard(player1, List.of(new CelestialFlare(), new KytheonsTactics()));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, bear.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Without two instants or sorceries in the graveyard it keeps sorcery timing")
    void noFlashTimingWithoutSpellMastery() {
        Permanent bear = addTappedCreature(player2);
        harness.setGraveyard(player1, List.of(new CelestialFlare()));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void untappedTargetSurvivesResolution() {
        Permanent creature = addTappedCreature(player2);
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, creature.getId());

        creature.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertNotInGraveyard(player2, "Charging Griffin");
        harness.assertInGraveyard(player1, "Swift Reckoning");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyOwnTappedCreature() {
        Permanent creature = addTappedCreature(player1);
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Charging Griffin");
    }

    @Test
    void cannotTargetTappedNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AlchemistsVial());
        artifact.tap();
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twoInstantsGrantFlashOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new CelestialFlare(), new CelestialFlare()));
        harness.forceActivePlayer(player2);
        assertFlashCastResolves();
    }

    @Test
    void twoSorceriesGrantFlash() {
        harness.setGraveyard(player1, List.of(new KytheonsTactics(), new KytheonsTactics()));
        assertFlashCastResolves();
    }

    private void assertFlashCastResolves() {
        Permanent creature = addTappedCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Charging Griffin");
    }

    @Test
    void creaturesDoNotCountForSpellMastery() {
        harness.setGraveyard(player1, List.of(new CelestialFlare(), new ChargingGriffin()));
        assertCannotCastDuringCombat();
    }

    @Test
    void opponentsGraveyardDoesNotGrantSpellMastery() {
        harness.setGraveyard(player1, List.of(new CelestialFlare()));
        harness.setGraveyard(player2, List.of(new KytheonsTactics(), new CelestialFlare()));
        assertCannotCastDuringCombat();
    }

    private void assertCannotCastDuringCombat() {
        Permanent creature = addTappedCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
