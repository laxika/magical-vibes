package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ClaraOswald;
import com.github.laxika.magicalvibes.cards.s.SonicScrewdriver;
import com.github.laxika.magicalvibes.cards.t.TheEleventhDoctor;
import com.github.laxika.magicalvibes.cards.z.ZygonInfiltrator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GallifreyCouncilChamber.class, ClaraOswald.class, SonicScrewdriver.class,
        TheEleventhDoctor.class, ZygonInfiltrator.class})
class GallifreyCouncilChamberTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and surveils one")
    void entersAndSurveilsOne() {
        Card topCard = new GallifreyCouncilChamber();
        Card nextCard = new GallifreyCouncilChamber();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new GallifreyCouncilChamber()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    void surveilCanLeaveTheCardOnTop() {
        Card topCard = new GallifreyCouncilChamber();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GallifreyCouncilChamber()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilWithAnEmptyLibraryCompletesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GallifreyCouncilChamber()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana casts either a Time Lord or Alien spell")
    void restrictedManaCastsEitherListedSubtypeSpell() {
        Permanent land = addReadyLand();
        produceRestrictedMana();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new TheEleventhDoctor()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        land.untap();
        produceRestrictedMana();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new ZygonInfiltrator()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast an unrelated subtype spell")
    void restrictedManaRejectsUnlistedSubtypeSpell() {
        addReadyLand();
        produceRestrictedMana();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player1, List.of(new ClaraOswald()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana activates an ability of a listed subtype source")
    void restrictedManaActivatesListedSubtypeAbility() {
        addReadyLand();
        harness.addToBattlefield(player1, new ZygonInfiltrator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClaraOswald());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        produceRestrictedMana();

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void anyColorCanPayForATimeLordAbility(String color) {
        Permanent land = addReadyLand();
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheEleventhDoctor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color);

        harness.activateAbility(player1, 1, null, doctor.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(doctor.isCantBeBlocked()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, doctor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCannotPayForAnArtifactAbility() {
        addReadyLand();
        Permanent screwdriver = harness.addToBattlefieldAndReturn(player1, new SonicScrewdriver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        produceRestrictedMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(screwdriver.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLand() {
        return harness.addToBattlefieldAndReturn(player1, new GallifreyCouncilChamber());
    }

    private void produceRestrictedMana() {
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
    }

}
