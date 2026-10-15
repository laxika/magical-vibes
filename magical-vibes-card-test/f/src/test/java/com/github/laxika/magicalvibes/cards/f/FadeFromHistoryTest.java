package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FadeFromHistory.class, Ornithopter.class, RuleOfLaw.class, GrizzlyBears.class})
class FadeFromHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Each player who controls an artifact or enchantment creates a Bear before those permanents are destroyed")
    void createsBearForEachEligiblePlayerThenDestroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castFadeFromHistory();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bearCount(player1.getId())).isEqualTo(1);
        assertThat(bearCount(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A player without an artifact or enchantment creates no Bear")
    void playerWithoutArtifactOrEnchantmentCreatesNoBear() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castFadeFromHistory();

        assertThat(bearCount(player1.getId())).isEqualTo(1);
        assertThat(bearCount(player2.getId())).isZero();
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Multiple artifacts and an enchantment still grant only one Bear")
    void multipleQualifyingPermanentsCreateOnlyOneBear() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new RuleOfLaw());

        castFadeFromHistory();

        assertThat(bearCount(player1.getId())).isEqualTo(1);
        assertThat(bearCount(player2.getId())).isZero();
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Rule of Law");
    }

    @Test
    @DisplayName("No Bears are created when neither player controls an artifact or enchantment")
    void emptyBattlefieldCreatesNoBears() {
        castFadeFromHistory();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Fade from History");
    }

    @Test
    @DisplayName("Eligibility is checked on resolution rather than when the spell is cast")
    void qualifyingPermanentEnteringBeforeResolutionGrantsBear() {
        harness.setHand(player1, List.of(new FadeFromHistory()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player2, new Ornithopter());

        harness.passBothPriorities();

        assertThat(bearCount(player1.getId())).isZero();
        assertThat(bearCount(player2.getId())).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    private long bearCount(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Bear"))
                .count();
    }

    private void castFadeFromHistory() {
        harness.setHand(player1, List.of(new FadeFromHistory()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
