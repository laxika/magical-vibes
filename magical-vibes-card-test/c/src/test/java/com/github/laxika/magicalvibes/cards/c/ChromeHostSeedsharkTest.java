package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindSpring;
import com.github.laxika.magicalvibes.cards.m.MondrakGloryDominus;
import com.github.laxika.magicalvibes.cards.u.UrnOfGodfire;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChromeHostSeedshark.class, Divination.class, GrizzlyBears.class,
        MindSpring.class, MondrakGloryDominus.class, UrnOfGodfire.class})
class ChromeHostSeedsharkTest extends BaseCardTest {

    @Test
    void noncreatureSpellIncubatesForItsManaValue() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void creatureSpellDoesNotIncubate() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void artifactSpellIncubatesBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.setHand(player1, List.of(new UrnOfGodfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Urn of Godfire");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotIncubate() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new UrnOfGodfire()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Incubator")).isZero();
        assertThat(countPermanents(player2, "Incubator")).isZero();
        harness.assertOnBattlefield(player2, "Urn of Godfire");
    }

    @Test
    void chosenXIsIncludedInTheSpellsManaValue() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
    }

    @Test
    void eachSeedsharkCreatesItsOwnIncubator() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.setHand(player1, List.of(new UrnOfGodfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Incubator")).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
    }

    @Test
    void doubledIncubatorsEachReceiveTheFullNumberOfCounters() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.addToBattlefield(player1, new MondrakGloryDominus());
        harness.setHand(player1, List.of(new UrnOfGodfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
    }

    @Test
    void incubatorFrontFaceHasTheIncubatorArtifactSubtype() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.setHand(player1, List.of(new UrnOfGodfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(incubator.getCard().getSubtypes()).extracting(subtype -> subtype.name())
                .contains("INCUBATOR");
        assertThat(incubator.isTransformed()).isFalse();
    }

    @Test
    void transformingRetainsCountersAndProducesTheCorrectBackFace() {
        harness.addToBattlefield(player1, new ChromeHostSeedshark());
        harness.setHand(player1, List.of(new UrnOfGodfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent incubator = findPermanent(player1, "Incubator");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(incubator);

        harness.activateAbility(player1, index, null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(incubator.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(incubator.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(incubator.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN);
        assertThat(incubator.getCard().getActivatedAbilities()).isEmpty();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(1);
        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian Token");
    }
}
