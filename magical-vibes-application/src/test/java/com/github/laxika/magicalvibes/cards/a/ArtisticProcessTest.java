package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArtisticProcessTest extends BaseCardTest {

    private static final int MANA_NEEDED = 5;

    

    @Nested
    @DisplayName("Mode 0: 6 damage to target creature")
    class TargetCreatureMode {

        @Test
        @DisplayName("Deals 6 damage to target creature")
        void deals6DamageToTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castSorcery(player1, 0, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Does not kill creatures with toughness greater than 6")
        void doesNotKillToughCreatures() {
            harness.addToBattlefield(player2, new EnormousBaloth());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            UUID targetId = harness.getPermanentId(player2, "Enormous Baloth");
            harness.castSorcery(player1, 0, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Enormous Baloth"));
        }
    }

    @Nested
    @DisplayName("Mode 1: 2 damage to each creature you don't control")
    class EachOpponentCreatureMode {

        @Test
        @DisplayName("Deals 2 damage to each creature you don't control")
        void damagesOpponentCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castSorcery(player1, 0, 1);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Does not kill opponent creatures with toughness greater than 2")
        void doesNotKillToughOpponentCreatures() {
            harness.addToBattlefield(player2, new GiantSpider());
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castSorcery(player1, 0, 1);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        }
    }

    @Nested
    @DisplayName("Mode 2: create Elemental token with flying and haste until end of turn")
    class TokenMode {

        @Test
        @DisplayName("Creates a 3/3 blue and red Elemental token with flying and haste until end of turn")
        void createsElementalTokenWithFlyingAndHaste() {
            harness.setHand(player1, List.of(new ArtisticProcess()));
            harness.addMana(player1, ManaColor.RED, MANA_NEEDED);

            harness.castSorcery(player1, 0, 2);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Elemental"))
                    .findFirst()
                    .orElseThrow();

            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(token.getGrantedKeywords()).contains(Keyword.HASTE);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        }
    }
}
