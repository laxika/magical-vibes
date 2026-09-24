package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoundationBreaker.class, AngelicChorus.class, LeoninScimitar.class, GrizzlyBears.class})
class FoundationBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may destroy a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");

        castBreaker(targetId);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Foundation Breaker");
    }

    @Test
    @DisplayName("ETB may destroy a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        castBreaker(targetId);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target intact")
    void mayCanBeDeclined() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");

        castBreaker(targetId);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("The ETB ability does not trigger when no artifact or enchantment exists")
    void doesNotTriggerWithoutLegalTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoundationBreaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Foundation Breaker");
    }

    @Test
    @DisplayName("Evoke uses {1}{G}, resolves the ETB ability, and sacrifices Foundation Breaker")
    void evokeDestroysArtifactAndSacrificesSelf() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.setHand(player1, List.of(new FoundationBreaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Foundation Breaker");
    }

    private void castBreaker(UUID targetId) {
        harness.setHand(player1, List.of(new FoundationBreaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
