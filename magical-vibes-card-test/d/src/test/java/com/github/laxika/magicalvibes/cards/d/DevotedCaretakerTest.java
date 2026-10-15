package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.e.EngulfingFlames;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.k.KirtarsWrath;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpray;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevotedCaretaker.class, AvenFlock.class, EngulfingFlames.class, Firebolt.class, Plains.class,
        KirtarsWrath.class, VolcanicSpray.class})
class DevotedCaretakerTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants protection from instants and sorceries to any permanent you control")
    void grantsProtectionToAnyPermanentYouControl() {
        addCreatureReady(player1, new DevotedCaretaker());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Plains");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent plains = findPermanent(player1, "Plains");
        assertThat(plains.getProtectionFromCardTypes())
                .contains(CardType.INSTANT, CardType.SORCERY);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(plains.getProtectionFromCardTypes()).doesNotContain(CardType.INSTANT, CardType.SORCERY);
    }

    @Test
    @DisplayName("The ability requires white mana and tapping Devoted Caretaker")
    void requiresWhiteManaAndTapping() {
        addCreatureReady(player1, new DevotedCaretaker());
        harness.addToBattlefield(player1, new Plains());

        UUID targetId = harness.getPermanentId(player1, "Plains");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Devoted Caretaker").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Protection from instants and sorceries prevents those spells from targeting the permanent")
    void protectionPreventsInstantAndSorceryTargeting() {
        addCreatureReady(player1, new DevotedCaretaker());
        harness.addToBattlefield(player1, new AvenFlock());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Aven Flock");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new EngulfingFlames(), new Firebolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThatThrownBy(() -> harness.castSorcery(player2, 1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("The ability cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentsPermanent() {
        addCreatureReady(player1, new DevotedCaretaker());
        harness.addToBattlefield(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentPermanentId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Devoted Caretaker can protect itself in response to a lethal sorcery")
    void protectsItselfInResponseToSorcery() {
        Permanent caretaker = addCreatureReady(player1, new DevotedCaretaker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Firebolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0, caretaker.getId());
        harness.activateAbility(player1, 0, 0, null, caretaker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devoted Caretaker");
        assertThat(caretaker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Firebolt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection gained in response makes an instant's target illegal")
    void protectsAgainstInstantAlreadyOnStack() {
        Permanent caretaker = addCreatureReady(player1, new DevotedCaretaker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new EngulfingFlames()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, caretaker.getId());
        harness.activateAbility(player1, 0, 0, null, caretaker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devoted Caretaker");
        assertThat(caretaker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Engulfing Flames");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Devoted Caretaker cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new DevotedCaretaker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID caretakerId = harness.getPermanentId(player1, "Devoted Caretaker");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, caretakerId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Devoted Caretaker cannot activate its tap ability again")
    void cannotActivateWhileTapped() {
        Permanent caretaker = addCreatureReady(player1, new DevotedCaretaker());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null, caretaker.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, caretaker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection from sorcery spells does not prevent untargeted destruction")
    void protectionDoesNotStopUntargetedDestruction() {
        Permanent caretaker = addCreatureReady(player1, new DevotedCaretaker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, caretaker.getId());
        harness.passBothPriorities();
        harness.castFromHand(player1, new KirtarsWrath(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devoted Caretaker");
        harness.assertInGraveyard(player1, "Devoted Caretaker");
    }

    @Test
    @DisplayName("Protection from sorcery spells prevents untargeted sorcery damage")
    void preventsUntargetedSorceryDamage() {
        Permanent caretaker = addCreatureReady(player1, new DevotedCaretaker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, caretaker.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new VolcanicSpray(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devoted Caretaker");
        assertThat(caretaker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
