package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MysticSubdual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavabrinkVenturer.class, GrizzlyBears.class, GrayOgre.class, LlanowarElves.class,
        Fireball.class, Mountain.class, MysticSubdual.class})
class LavabrinkVenturerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving awaits an odd/even choice")
    void resolvingAwaitsParityChoice() {
        harness.castFromHand(player1, new LavabrinkVenturer(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Protection follows the chosen even quality")
    void protectsFromEvenManaValues() {
        Permanent venturer = castAndChoose("EVEN");

        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrizzlyBears()))).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new LlanowarElves()))).isFalse();
    }

    @Test
    @DisplayName("Protection follows the chosen odd quality")
    void protectsFromOddManaValues() {
        Permanent venturer = castAndChoose("ODD");

        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrayOgre()))).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrizzlyBears()))).isFalse();
    }

    @Test
    @DisplayName("No protection applies before a quality is chosen")
    void hasNoProtectionBeforeChoice() {
        harness.addToBattlefield(player1, new LavabrinkVenturer());
        Permanent venturer = findPermanent(player1, "Lavabrink Venturer");

        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrizzlyBears()))).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new LlanowarElves()))).isFalse();
    }

    @Test
    void zeroManaValueIsEven() {
        Permanent venturer = castAndChoose("EVEN");

        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new Mountain()))).isTrue();
    }

    @Test
    void oddProtectionDoesNotProtectFromZeroManaValue() {
        Permanent venturer = castAndChoose("ODD");

        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new Mountain()))).isFalse();
    }

    @Test
    void chosenParityRestrictsBlockers() {
        Permanent venturer = castAndChoose("EVEN");
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new GrayOgre());

        assertThat(bls.canBlockAttacker(gd, bears, venturer, gd.playerBattlefields.get(player2.getId())))
                .isFalse();
        assertThat(bls.canBlockAttacker(gd, ogre, venturer, gd.playerBattlefields.get(player2.getId())))
                .isTrue();
    }

    @Test
    void preventsCombatDamageFromEvenSource() {
        castAndChoose("EVEN");
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Lavabrink Venturer");
        assertThat(findPermanent(player1, "Lavabrink Venturer").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void evenProtectionPreventsEvenAuraTargeting() {
        Permanent venturer = castAndChoose("EVEN");
        harness.setHand(player1, List.of(new MysticSubdual()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, venturer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void losingAllAbilitiesRemovesChosenParityProtection() {
        Permanent venturer = castAndChoose("ODD");
        harness.setHand(player1, List.of(new MysticSubdual()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        harness.castEnchantment(player1, 0, venturer.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mystic Subdual");
        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrayOgre()))).isFalse();
    }

    @Test
    void evenProtectionIncludesChosenXInSpellManaValue() {
        Permanent venturer = castAndChoose("EVEN");
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, venturer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void oddProtectionAllowsEvenManaValueXSpell() {
        Permanent venturer = castAndChoose("ODD");
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.ensurePriority(player1);

        harness.castAndResolveSorcery(player1, 0, 1, venturer.getId());

        harness.assertOnBattlefield(player1, "Lavabrink Venturer");
        assertThat(venturer.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent castAndChoose(String parity) {
        harness.castFromHand(player1, new LavabrinkVenturer(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, parity);

        return findPermanent(player1, "Lavabrink Venturer");
    }
}
