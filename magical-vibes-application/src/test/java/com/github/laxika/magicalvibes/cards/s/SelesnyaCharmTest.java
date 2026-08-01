package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelesnyaCharmTest extends BaseCardTest {

    private void addGW() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Nested
    @DisplayName("Mode 0: Target creature gets +2/+2 and gains trample until end of turn")
    class PumpMode {

        @Test
        @DisplayName("Grants +2/+2 and trample to target creature")
        void grantsBoostAndTrample() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            Permanent bears = findPermanent(player1, "Grizzly Bears");
            assertThat(bears.getPowerModifier()).isEqualTo(2);
            assertThat(bears.getToughnessModifier()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Boost and trample wear off at end of turn")
        void wearsOffAtEndOfTurn() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bears = findPermanent(player1, "Grizzly Bears");
            assertThat(bears.getPowerModifier()).isZero();
            assertThat(bears.getToughnessModifier()).isZero();
            assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        }
    }

    @Nested
    @DisplayName("Mode 1: Exile target creature with power 5 or greater")
    class ExileMode {

        @Test
        @DisplayName("Exiles a creature with power 5 or greater")
        void exilesHighPowerCreature() {
            harness.addToBattlefield(player2, new CrawWurm());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            UUID targetId = harness.getPermanentId(player2, "Craw Wurm");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Craw Wurm");
            assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Craw Wurm"));
        }

        @Test
        @DisplayName("Cannot target a creature with power less than 5")
        void cannotTargetLowPowerCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new CrawWurm());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Create a 2/2 white Knight token with vigilance")
    class TokenMode {

        @Test
        @DisplayName("Creates a 2/2 white Knight with vigilance")
        void createsKnightToken() {
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().getName().equals("Knight"))
                    .singleElement()
                    .satisfies(knight -> {
                        assertThat(knight.getCard().getPower()).isEqualTo(2);
                        assertThat(knight.getCard().getToughness()).isEqualTo(2);
                        assertThat(knight.getCard().isToken()).isTrue();
                        assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
                    });
            harness.assertInGraveyard(player1, "Selesnya Charm");
        }
    }
}
