package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Dovescape;
import com.github.laxika.magicalvibes.cards.p.PsychoticFury;
import com.github.laxika.magicalvibes.cards.s.SealOfFire;
import com.github.laxika.magicalvibes.cards.w.WritOfPassage;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AzoriusFirstWing.class, Dovescape.class, AzoriusHerald.class,
        WritOfPassage.class, SealOfFire.class, PsychoticFury.class})
class AzoriusFirstWingTest extends BaseCardTest {

    @Test
    @DisplayName("Azorius First-Wing has protection from enchantments")
    void hasProtectionFromEnchantments() {
        Permanent firstWing = harness.addToBattlefieldAndReturn(player1, new AzoriusFirstWing());
        Permanent enchantment = new Permanent(new Dovescape());
        Permanent creature = new Permanent(new AzoriusHerald());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, firstWing, enchantment)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, firstWing, creature)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Neither player's Aura can target Azorius First-Wing")
    void auraCannotTargetFirstWing(boolean sameController) {
        Permanent firstWing = harness.addToBattlefieldAndReturn(
                sameController ? player1 : player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new WritOfPassage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, firstWing.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Writ of Passage");
    }

    @Test
    @DisplayName("An illegally attached Aura is put into its owner's graveyard")
    void protectionRemovesAnAttachedAura() {
        Permanent firstWing = harness.addToBattlefieldAndReturn(player1, new AzoriusFirstWing());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new WritOfPassage());
        aura.setAttachedTo(firstWing.getId());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Writ of Passage");
        harness.assertInGraveyard(player2, "Writ of Passage");
        harness.assertOnBattlefield(player1, "Azorius First-Wing");
    }

    @Test
    @DisplayName("An enchantment's activated ability cannot target Azorius First-Wing")
    void enchantmentAbilityCannotTargetFirstWing() {
        harness.addToBattlefield(player1, new SealOfFire());
        Permanent firstWing = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, firstWing.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        harness.assertOnBattlefield(player1, "Seal of Fire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection from enchantments does not stop a targeted instant")
    void instantCanTargetFirstWing() {
        Permanent firstWing = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new PsychoticFury()));
        harness.setLibrary(player1, List.of(new AzoriusFirstWing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, firstWing.getId());

        assertThat(gqs.hasKeyword(gd, firstWing, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.assertInHand(player1, "Azorius First-Wing");
    }
}
