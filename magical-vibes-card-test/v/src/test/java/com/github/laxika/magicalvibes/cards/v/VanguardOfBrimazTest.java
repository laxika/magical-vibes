package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VanguardOfBrimaz.class, GiantGrowth.class, Shock.class})
class VanguardOfBrimazTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Vanguard of Brimaz creates a vigilant Cat Soldier token")
    void targetingVanguardCreatesCatSoldier() {
        harness.addToBattlefield(player1, new VanguardOfBrimaz());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID vanguardId = harness.getPermanentId(player1, "Vanguard of Brimaz");
        harness.castInstant(player1, 0, vanguardId);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cat Soldier")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Cat Soldier");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A spell that does not target Vanguard of Brimaz does not create a token")
    void spellNotTargetingVanguardDoesNotCreateCatSoldier() {
        harness.addToBattlefield(player1, new VanguardOfBrimaz());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat Soldier")).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Vanguard of Brimaz does not create a token")
    void opponentsSpellDoesNotCreateCatSoldier() {
        harness.addToBattlefield(player1, new VanguardOfBrimaz());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID vanguardId = harness.getPermanentId(player1, "Vanguard of Brimaz");
        harness.castInstant(player2, 0, vanguardId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat Soldier")).isZero();
    }

    @Test
    @DisplayName("Heroic resolves before the targeting spell, even when that spell kills Vanguard")
    void tokenIsCreatedBeforeLethalTargetingSpellResolves() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new VanguardOfBrimaz());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, vanguard.getId());
        assertThat(countPermanents(player1, "Cat Soldier")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat Soldier")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Vanguard of Brimaz");

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Vanguard of Brimaz");
        harness.assertInGraveyard(player1, "Vanguard of Brimaz");
        assertThat(countPermanents(player1, "Cat Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("Heroic still creates a token after Vanguard dies in response")
    void triggerSurvivesSourceRemovalAndTargetingSpellLosingItsTarget() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new VanguardOfBrimaz());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, vanguard.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, vanguard.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vanguard of Brimaz");
        assertThat(countPermanents(player1, "Cat Soldier")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cat Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Cat Soldier")).isZero();
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    @DisplayName("Only the targeted Vanguard triggers, and each targeting spell creates another token")
    void multipleVanguardsAndRepeatedTargetingSpells() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new VanguardOfBrimaz());
        harness.addToBattlefield(player1, new VanguardOfBrimaz());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, vanguard.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cat Soldier")).isEqualTo(1);

        harness.castInstant(player1, 0, vanguard.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cat Soldier")).isEqualTo(2);
        assertThat(countPermanents(player2, "Cat Soldier")).isZero();
    }
}
