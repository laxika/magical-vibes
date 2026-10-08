package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArtisticProcess.class, EnormousBaloth.class, GiantSpider.class, GrizzlyBears.class, Unsummon.class})
class ArtisticProcessTest extends BaseCardTest {

    private static final int MANA_NEEDED = 5;

    @Nested
    @CardUsed({ArtisticProcess.class, EnormousBaloth.class, GiantSpider.class, GrizzlyBears.class, Unsummon.class})
    @DisplayName("Mode 0: 6 damage to target creature")
    class TargetCreatureMode {

        @Test
        void doesNotResolveAnotherModeWhenTargetLeavesBattlefield() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent other = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.setHand(player2, List.of(new Unsummon()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castSorcery(player1, 0, 0, target.getId());
            harness.castAndResolveInstant(player2, 0, target.getId());
            harness.passBothPriorities();

            harness.assertInHand(player2, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Artistic Process");
            assertThat(other.getMarkedDamage()).isZero();
            assertThat(countPermanents(player1, "Elemental")).isZero();
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
        }

        @Test
        void canTargetOwnCreatureAndDealsExactlySixDamage() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new EnormousBaloth());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castAndResolveSorcery(player1, 0, 0, target.getId());

            assertThat(target.getMarkedDamage()).isEqualTo(6);
            harness.assertOnBattlefield(player1, "Enormous Baloth");
            assertThat(countPermanents(player1, "Elemental")).isZero();
        }

        @Test
        @DisplayName("Deals 6 damage to target creature")
        void deals6DamageToTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castAndResolveSorcery(player1, 0, 0, targetId);

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Does not kill creatures with toughness greater than 6")
        void doesNotKillToughCreatures() {
            harness.addToBattlefield(player2, new EnormousBaloth());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            UUID targetId = harness.getPermanentId(player2, "Enormous Baloth");
            harness.castAndResolveSorcery(player1, 0, 0, targetId);

            harness.assertOnBattlefield(player2, "Enormous Baloth");
        }
    }

    @Nested
    @CardUsed({ArtisticProcess.class, EnormousBaloth.class, GiantSpider.class, GrizzlyBears.class})
    @DisplayName("Mode 1: 2 damage to each creature you don't control")
    class EachOpponentCreatureMode {

        @Test
        void damagesAllOpponentCreaturesAndLeavesOwnCreaturesAndPlayersUnharmed() {
            Permanent own = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
            Permanent first = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
            Permanent second = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castAndResolveSorcery(player1, 0, 1);

            assertThat(own.getMarkedDamage()).isZero();
            assertThat(first.getMarkedDamage()).isEqualTo(2);
            assertThat(second.getMarkedDamage()).isEqualTo(2);
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            assertThat(countPermanents(player1, "Elemental")).isZero();
        }

        @Test
        void massDamageModeCanResolveWithoutCreatures() {
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castAndResolveSorcery(player1, 0, 1);

            harness.assertInGraveyard(player1, "Artistic Process");
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Deals 2 damage to each creature you don't control")
        void damagesOpponentCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castAndResolveSorcery(player1, 0, 1);

            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Does not kill opponent creatures with toughness greater than 2")
        void doesNotKillToughOpponentCreatures() {
            harness.addToBattlefield(player2, new GiantSpider());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castAndResolveSorcery(player1, 0, 1);

            harness.assertOnBattlefield(player2, "Giant Spider");
        }
    }

    @Nested
    @CardUsed({ArtisticProcess.class})
    @DisplayName("Mode 2: create Elemental token with flying and haste until end of turn")
    class TokenMode {

        @Test
        void tokenLosesHasteAfterCleanupButRetainsFlyingAndBothColors() {
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castAndResolveSorcery(player1, 0, 2);

            Permanent token = findPermanent(player1, "Elemental");
            assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);

            harness.forceStep(TurnStep.END_STEP);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Elemental");
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        }

        @Test
        @DisplayName("Creates a 3/3 blue and red Elemental token with flying and haste until end of turn")
        void createsElementalTokenWithFlyingAndHaste() {
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castAndResolveSorcery(player1, 0, 2);

            GameData gd = harness.getGameData();
            Permanent token = findPermanent(player1, "Elemental");

            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(token.getGrantedKeywords()).contains(Keyword.HASTE);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        }
    }
}
