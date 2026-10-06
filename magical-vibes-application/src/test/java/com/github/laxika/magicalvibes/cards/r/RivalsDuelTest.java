package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.OrderOfTheGoldenCricket;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.cards.s.StinkdrinkerBandit;
import com.github.laxika.magicalvibes.cards.w.WarSpikeChangeling;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RivalsDuel.class, OrderOfTheGoldenCricket.class, PricklyBoggart.class,
        WarSpikeChangeling.class, StinkdrinkerBandit.class})
class RivalsDuelTest extends BaseCardTest {

    @Test
    @DisplayName("Two creatures sharing no creature types fight each other")
    void creaturesSharingNoTypesFight() {
        harness.addToBattlefield(player1, new OrderOfTheGoldenCricket());
        harness.addToBattlefield(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID cricketId = harness.getPermanentId(player1, "Order of the Golden Cricket");
        UUID boggartId = harness.getPermanentId(player2, "Prickly Boggart");
        harness.castAndResolveSorcery(player1, 0, List.of(cricketId, boggartId));

        // Cricket (2/2) deals 2 to Boggart (1/1) which dies; Boggart's 1 damage leaves the Cricket alive.
        harness.assertInGraveyard(player2, "Prickly Boggart");
        harness.assertOnBattlefield(player1, "Order of the Golden Cricket");
    }

    @Test
    @DisplayName("Spell does not resolve when both targets gain shroud")
    void doesNotResolveWhenBothTargetsGainShroud() {
        Permanent cricket = harness.addToBattlefieldAndReturn(player1, new OrderOfTheGoldenCricket());
        Permanent boggart = harness.addToBattlefieldAndReturn(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(cricket.getId(), boggart.getId()));

        cricket.getGrantedKeywords().add(Keyword.SHROUD);
        boggart.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Order of the Golden Cricket");
        harness.assertOnBattlefield(player2, "Prickly Boggart");
        assertThat(cricket.getMarkedDamage()).isZero();
        assertThat(boggart.getMarkedDamage()).isZero();
        assertThat(gameLogContains("fizzles (illegal target)")).isTrue();
    }

    @Test
    @DisplayName("Neither creature fights when one target gains shroud")
    void creaturesDoNotFightWhenOneTargetGainsShroud() {
        Permanent cricket = harness.addToBattlefieldAndReturn(player1, new OrderOfTheGoldenCricket());
        Permanent boggart = harness.addToBattlefieldAndReturn(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(cricket.getId(), boggart.getId()));

        boggart.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Order of the Golden Cricket");
        harness.assertOnBattlefield(player2, "Prickly Boggart");
        assertThat(cricket.getMarkedDamage()).isZero();
        assertThat(boggart.getMarkedDamage()).isZero();
        assertThat(gameLogContains("fizzles (illegal target)")).isFalse();
    }

    @Test
    @DisplayName("Cannot choose two creatures that share a creature type")
    void cannotTargetCreaturesSharingType() {
        harness.addToBattlefield(player1, new OrderOfTheGoldenCricket());
        harness.addToBattlefield(player2, new OrderOfTheGoldenCricket());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID firstCricket = harness.getPermanentId(player1, "Order of the Golden Cricket");
        UUID secondCricket = harness.getPermanentId(player2, "Order of the Golden Cricket");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(firstCricket, secondCricket)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature types");
    }

    @Test
    @DisplayName("A Changeling shares every creature type, so it cannot be paired with another creature")
    void cannotTargetChangelingWithAnyCreature() {
        harness.addToBattlefield(player1, new WarSpikeChangeling());
        harness.addToBattlefield(player2, new OrderOfTheGoldenCricket());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID changelingId = harness.getPermanentId(player1, "War-Spike Changeling");
        UUID cricketId = harness.getPermanentId(player2, "Order of the Golden Cricket");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(changelingId, cricketId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature types");
    }

    @Test
    @DisplayName("Creatures controlled by the same player may fight")
    void creaturesControlledBySamePlayerMayFight() {
        harness.addToBattlefield(player1, new OrderOfTheGoldenCricket());
        harness.addToBattlefield(player1, new PricklyBoggart());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID cricketId = harness.getPermanentId(player1, "Order of the Golden Cricket");
        UUID boggartId = harness.getPermanentId(player1, "Prickly Boggart");
        harness.castAndResolveSorcery(player1, 0, List.of(cricketId, boggartId));

        harness.assertOnBattlefield(player1, "Order of the Golden Cricket");
        harness.assertInGraveyard(player1, "Prickly Boggart");
    }

    @Test
    @DisplayName("Spell fizzles when the targets share a creature type before resolution")
    void spellFizzlesWhenTargetsStartSharingCreatureType() {
        Permanent cricket = harness.addToBattlefieldAndReturn(player1, new OrderOfTheGoldenCricket());
        Permanent boggart = harness.addToBattlefieldAndReturn(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(cricket.getId(), boggart.getId()));
        boggart.getGrantedSubtypes().add(CardSubtype.KNIGHT);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Order of the Golden Cricket");
        harness.assertOnBattlefield(player2, "Prickly Boggart");
        assertThat(cricket.getMarkedDamage()).isZero();
        assertThat(boggart.getMarkedDamage()).isZero();
        assertThat(gameLogContains("fizzles (illegal target)")).isTrue();
    }

    @Test
    @DisplayName("Fight damage is simultaneous even when a creature has first strike")
    void firstStrikeDoesNotPreventMutualLethalFightDamage() {
        Permanent cricket = harness.addToBattlefieldAndReturn(player1, new OrderOfTheGoldenCricket());
        Permanent bandit = harness.addToBattlefieldAndReturn(player2, new StinkdrinkerBandit());
        cricket.getGrantedKeywords().add(Keyword.FIRST_STRIKE);
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(cricket.getId(), bandit.getId()));

        harness.assertInGraveyard(player1, "Order of the Golden Cricket");
        harness.assertInGraveyard(player2, "Stinkdrinker Bandit");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Neither creature fights when the first target gains shroud")
    void creaturesDoNotFightWhenFirstTargetGainsShroud() {
        Permanent cricket = harness.addToBattlefieldAndReturn(player1, new OrderOfTheGoldenCricket());
        Permanent boggart = harness.addToBattlefieldAndReturn(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new RivalsDuel()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(cricket.getId(), boggart.getId()));
        cricket.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Order of the Golden Cricket");
        harness.assertOnBattlefield(player2, "Prickly Boggart");
        assertThat(cricket.getMarkedDamage()).isZero();
        assertThat(boggart.getMarkedDamage()).isZero();
        assertThat(gameLogContains("fizzles (illegal target)")).isFalse();
    }
}
