package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.d.Downsize;
import com.github.laxika.magicalvibes.cards.g.GolgariLonglegs;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelesnyaCharm.class, AxebaneStag.class, DrudgeBeetle.class,
        Downsize.class, GolgariLonglegs.class, Plains.class})
class SelesnyaCharmTest extends BaseCardTest {

    private void addGW() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Nested
    @DisplayName("Mode 0: Target creature gets +2/+2 and gains trample until end of turn")
    @CardUsed({SelesnyaCharm.class, DrudgeBeetle.class, GolgariLonglegs.class, Plains.class})
    class PumpMode {

        @Test
        void canBoostAnOpponentsCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            harness.castInstant(player1, 0, 0, target.getId());
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }

        @Test
        void cannotBoostANoncreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new Plains());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void removedTargetDoesNotCauseAnotherModeToResolve() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariLonglegs());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            harness.setHand(player2, List.of(new SelesnyaCharm()));
            addGW();
            harness.addMana(player2, ManaColor.GREEN, 1);
            harness.addMana(player2, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, target.getId());
            harness.castInstant(player2, 0, 1, target.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Golgari Longlegs");
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
            harness.assertInGraveyard(player1, "Selesnya Charm");
        }

        @Test
        @DisplayName("Grants +2/+2 and trample to target creature")
        void grantsBoostAndTrample() {
            harness.addToBattlefield(player1, new DrudgeBeetle());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            UUID targetId = harness.getPermanentId(player1, "Drudge Beetle");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            Permanent beetle = findPermanent(player1, "Drudge Beetle");
            assertThat(beetle.getPowerModifier()).isEqualTo(2);
            assertThat(beetle.getToughnessModifier()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, beetle, Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Boost and trample wear off at end of turn")
        void wearsOffAtEndOfTurn() {
            harness.addToBattlefield(player1, new DrudgeBeetle());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            UUID targetId = harness.getPermanentId(player1, "Drudge Beetle");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(player2, TurnStep.UPKEEP);

            Permanent beetle = findPermanent(player1, "Drudge Beetle");
            assertThat(beetle.getPowerModifier()).isZero();
            assertThat(beetle.getToughnessModifier()).isZero();
            assertThat(gqs.hasKeyword(gd, beetle, Keyword.TRAMPLE)).isFalse();
        }
    }

    @Nested
    @DisplayName("Mode 1: Exile target creature with power 5 or greater")
    @CardUsed({SelesnyaCharm.class, AxebaneStag.class, DrudgeBeetle.class,
            Downsize.class, GolgariLonglegs.class})
    class ExileMode {

        @Test
        void canExileOwnCreatureWithExactlyFivePower() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariLonglegs());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Golgari Longlegs");
            assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Golgari Longlegs"));
            harness.assertInGraveyard(player1, "Selesnya Charm");
        }

        @Test
        void doesNotExileWhenPowerDropsBelowFiveInResponse() {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariLonglegs());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            harness.setHand(player2, List.of(new Downsize()));
            addGW();
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 1, target.getId());
            harness.castInstant(player2, 0, target.getId());
            harness.passBothPriorities();
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
            assertThat(gd.exiledCards).isEmpty();
            harness.assertInGraveyard(player1, "Selesnya Charm");
            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        }

        @Test
        void usesBoostedPowerWhenChoosingExileTarget() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
            harness.setHand(player1, List.of(new SelesnyaCharm(), new SelesnyaCharm(), new SelesnyaCharm()));
            harness.addMana(player1, ManaColor.GREEN, 3);
            harness.addMana(player1, ManaColor.WHITE, 3);

            harness.castInstant(player1, 0, 0, target.getId());
            harness.passBothPriorities();
            harness.castInstant(player1, 0, 0, target.getId());
            harness.passBothPriorities();
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Drudge Beetle");
            assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Drudge Beetle"));
        }

        @Test
        @DisplayName("Exiles a creature with power 5 or greater")
        void exilesHighPowerCreature() {
            harness.addToBattlefield(player2, new AxebaneStag());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            UUID targetId = harness.getPermanentId(player2, "Axebane Stag");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Axebane Stag");
            assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Axebane Stag"));
        }

        @Test
        @DisplayName("Cannot target a creature with power less than 5")
        void cannotTargetLowPowerCreature() {
            harness.addToBattlefield(player2, new DrudgeBeetle());
            harness.addToBattlefield(player1, new AxebaneStag());
            harness.setHand(player1, List.of(new SelesnyaCharm()));
            addGW();

            UUID targetId = harness.getPermanentId(player2, "Drudge Beetle");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Create a 2/2 white Knight token with vigilance")
    @CardUsed({SelesnyaCharm.class})
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
                        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
                        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
                    });
            harness.assertInGraveyard(player1, "Selesnya Charm");
        }
    }
}
