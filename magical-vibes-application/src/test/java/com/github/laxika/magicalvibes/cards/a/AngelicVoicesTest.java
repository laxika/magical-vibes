package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeHorse;
import com.github.laxika.magicalvibes.cards.k.KeepersOfTheFaith;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.u.Urborg;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicVoices.class, KeepersOfTheFaith.class, BronzeHorse.class,
        KoboldsOfKherKeep.class, Urborg.class})
class AngelicVoicesTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts own white and artifact creatures when the condition is met")
    void boostsOwnWhiteAndArtifactCreatures() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player1, new KeepersOfTheFaith());
        harness.addToBattlefield(player1, new BronzeHorse());

        Permanent keepers = findPermanent(player1, "Keepers of the Faith");
        Permanent bronzeHorse = findPermanent(player1, "Bronze Horse");

        assertThat(gqs.getEffectivePower(gd, keepers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keepers)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bronzeHorse)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bronzeHorse)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost own creatures while controlling a nonartifact nonwhite creature")
    void conditionTurnsOffForNonartifactNonwhiteCreature() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player1, new KeepersOfTheFaith());
        harness.addToBattlefield(player1, new BronzeHorse());
        harness.addToBattlefield(player1, new KoboldsOfKherKeep());

        Permanent keepers = findPermanent(player1, "Keepers of the Faith");
        Permanent bronzeHorse = findPermanent(player1, "Bronze Horse");
        Permanent kobolds = findPermanent(player1, "Kobolds of Kher Keep");

        assertThat(gqs.getEffectivePower(gd, keepers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, keepers)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bronzeHorse)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bronzeHorse)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, kobolds)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, kobolds)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost creatures controlled by an opponent")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player1, new KeepersOfTheFaith());
        harness.addToBattlefield(player2, new KoboldsOfKherKeep());

        Permanent keepers = findPermanent(player1, "Keepers of the Faith");
        Permanent opponentKobolds = findPermanent(player2, "Kobolds of Kher Keep");

        assertThat(gqs.getEffectivePower(gd, keepers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keepers)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentKobolds)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentKobolds)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonartifact nonwhite noncreature does not turn off the boost")
    void conditionIgnoresNoncreaturePermanents() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player1, new KeepersOfTheFaith());
        harness.addToBattlefield(player1, new BronzeHorse());
        harness.addToBattlefield(player1, new Urborg());

        Permanent keepers = findPermanent(player1, "Keepers of the Faith");
        Permanent bronzeHorse = findPermanent(player1, "Bronze Horse");

        assertThat(gqs.getEffectivePower(gd, keepers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keepers)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bronzeHorse)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bronzeHorse)).isEqualTo(5);
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Boosts itself when Opalescence makes it a creature")
    void boostsItselfWhenItBecomesACreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent voices = harness.addToBattlefieldAndReturn(player1, new AngelicVoices());

        assertThat(gqs.isCreature(gd, voices)).isTrue();
        assertThat(gqs.getEffectivePower(gd, voices)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, voices)).isEqualTo(5);
    }
}
