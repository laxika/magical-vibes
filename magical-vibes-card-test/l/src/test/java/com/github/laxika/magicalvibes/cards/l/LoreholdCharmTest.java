package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoreholdCharm.class, EnormousBaloth.class, FountainOfYouth.class, GrizzlyBears.class})
class LoreholdCharmTest extends BaseCardTest {

    private void addRW() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    

    @Nested
    @DisplayName("Mode 0: Each opponent sacrifices a nontoken artifact")
    @CardUsed({LoreholdCharm.class, FountainOfYouth.class, GrizzlyBears.class})
    class SacrificeArtifactMode {

        @Test
        void opponentChoosesWhichArtifactToSacrifice() {
            Permanent kept = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
            Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();
            harness.handleMultiplePermanentsChosen(player2, List.of(sacrificed.getId()));

            assertThat(gd.playerBattlefields.get(player2.getId())).contains(kept).doesNotContain(sacrificed);
            harness.assertInGraveyard(player2, "Fountain of Youth");
            harness.assertOnBattlefield(player1, "Fountain of Youth");
        }

        @Test
        void tokenArtifactsAndNonartifactCreaturesAreNotSacrificed() {
            Card token = new FountainOfYouth();
            token.setToken(true);
            Permanent tokenPermanent = harness.addToBattlefieldAndReturn(player2, token);
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).contains(tokenPermanent);
            assertThat(countPermanents(player2, "Fountain of Youth")).isEqualTo(1);
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Fountain of Youth");
        }

        @Test
        void canResolveWithoutOpponentArtifacts() {
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Fountain of Youth");
            harness.assertInGraveyard(player1, "Lorehold Charm");
        }

        @Test
        @DisplayName("Opponent sacrifices their only artifact")
        void opponentSacrificesArtifact() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Fountain of Youth");
            harness.assertInGraveyard(player2, "Fountain of Youth");
        }
    }

    @Nested
    @DisplayName("Mode 1: Reanimate an artifact or creature card with mana value 2 or less")
    @CardUsed({LoreholdCharm.class, GrizzlyBears.class, EnormousBaloth.class, FountainOfYouth.class})
    class ReanimateMode {

        @Test
        void returnsNoncreatureArtifactUntapped() {
            Card fountain = new FountainOfYouth();
            harness.setGraveyard(player1, List.of(fountain));
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 1, fountain.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player1, "Fountain of Youth");
            assertThat(findPermanent(player1, "Fountain of Youth").isTapped()).isFalse();
        }

        @Test
        void cannotTargetOpponentsGraveyard() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(bears));
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotTargetLowManaValueInstant() {
            Card otherCharm = new LoreholdCharm();
            harness.setGraveyard(player1, List.of(otherCharm));
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, otherCharm.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void removedGraveyardTargetIsNotReturned() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();
            harness.castInstant(player1, 0, 1, bears.getId());
            harness.setGraveyard(player1, List.of());
            harness.setExile(player1, List.of(bears));

            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
            harness.assertInGraveyard(player1, "Lorehold Charm");
        }

        @Test
        @DisplayName("Returns a mana value 2 creature card from graveyard to battlefield")
        void reanimatesLowManaValueCreature() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a card with mana value greater than 2")
        void cannotTargetHighManaValue() {
            Card baloth = new EnormousBaloth();
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(baloth, bears));
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, baloth.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Creatures you control get +1/+1 and gain trample")
    @CardUsed({LoreholdCharm.class, GrizzlyBears.class})
    class AnthemMode {

        @Test
        void laterCreaturesDoNotReceiveTheBonus() {
            Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();
            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, existing, Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, later, Keyword.TRAMPLE)).isFalse();
        }

        @Test
        void bonusAndTrampleExpireAtCleanup() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();
            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        }

        @Test
        @DisplayName("Boosts your creatures and grants trample")
        void boostsAndGrantsTrample() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent bears = findPermanent(player1, "Grizzly Bears");
            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Does not boost opponent creatures")
        void doesNotBoostOpponentCreatures() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new LoreholdCharm()));
            addRW();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent bears = findPermanent(player2, "Grizzly Bears");
            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        }
    }
}
