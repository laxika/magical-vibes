package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaneOfProgress.class, GrizzlyBears.class, Ornithopter.class, RuleOfLaw.class})
class BaneOfProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts and enchantments and gets a counter for each")
    void destroysArtifactsAndEnchantmentsAndGetsCounters() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bane = castBaneOfProgress();
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy creatures or put counters on itself without matching permanents")
    void ignoresNonArtifactAndNonEnchantmentPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bane = castBaneOfProgress();
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts only permanents actually destroyed")
    void doesNotCountIndestructiblePermanents() {
        Permanent indestructibleArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        indestructibleArtifact.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player1, new RuleOfLaw());

        Permanent bane = castBaneOfProgress();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructibleArtifact);
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not destroy creatures or get counters when there are no matching permanents")
    void ignoresCreaturesAndCountsNoDestroyedPermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bane = castBaneOfProgress();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Regenerated artifacts survive and do not contribute counters")
    void doesNotCountRegeneratedPermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        artifact.setRegenerationShield(1);
        harness.addToBattlefield(player1, new RuleOfLaw());

        Permanent bane = castBaneOfProgress();

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Rule of Law");
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts multiple artifacts and enchantments on both battlefields")
    void countsAllDestroyedPermanentsAcrossControllers() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new RuleOfLaw());

        Permanent bane = castBaneOfProgress();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Rule of Law");
        harness.assertInGraveyard(player2, "Rule of Law");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private Permanent castBaneOfProgress() {
        harness.setHand(player1, List.of(new BaneOfProgress()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Bane of Progress");
    }
}
