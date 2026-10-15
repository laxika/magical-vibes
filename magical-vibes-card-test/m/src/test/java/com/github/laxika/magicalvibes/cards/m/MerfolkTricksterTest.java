package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BlindingMage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfGrace;
import com.github.laxika.magicalvibes.cards.k.KnightOfMalice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
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

@CardUsed({MerfolkTrickster.class, AirElemental.class, BlindingMage.class, GrizzlyBears.class,
        KnightOfGrace.class, KnightOfMalice.class})
class MerfolkTricksterTest extends BaseCardTest {

    @CardUsed({MerfolkTrickster.class, AirElemental.class, BlindingMage.class, GrizzlyBears.class})
    @Nested
    @DisplayName("ETB trigger")
    class EnterTheBattlefield {

        @Test
        @DisplayName("ETB trigger goes on the stack when Merfolk Trickster enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castTrickster(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Merfolk Trickster");
        }

        @Test
        @DisplayName("Taps target creature an opponent controls")
        void tapsTargetCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            assertThat(bears.isTapped()).isFalse();

            castTrickster(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Target creature loses all keywords until end of turn")
        void targetLosesKeywords() {
            Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();

            castTrickster(player2, "Air Elemental");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("Target creature loses activated abilities until end of turn")
        void targetLosesActivatedAbilities() {
            Permanent mage = addCreatureReady(player2, new BlindingMage());

            castTrickster(player2, "Blinding Mage");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            mage.untap();
            harness.addMana(player2, ManaColor.WHITE, 1);
            UUID legalTarget = harness.getPermanentId(player1, "Merfolk Trickster");

            assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, legalTarget))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Merfolk Trickster enters the battlefield")
        void tricksterEntersBattlefield() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castTrickster(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            harness.assertOnBattlefield(player1, "Merfolk Trickster");
        }
    }

    @CardUsed({MerfolkTrickster.class, AirElemental.class, BlindingMage.class, GrizzlyBears.class})
    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID opponentBearId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castFromHand(player1, new MerfolkTrickster(), "{U}{U}");
            harness.passBothPriorities();

            assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBearId))
                    .isInstanceOf(IllegalStateException.class);
            harness.handlePermanentChosen(player1, opponentBearId);
            resolveAllTriggers();

            assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
            assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
        }
    }

    @CardUsed({MerfolkTrickster.class, AirElemental.class, BlindingMage.class, GrizzlyBears.class})
    @Nested
    @DisplayName("End of turn cleanup")
    class EndOfTurnCleanup {

        @Test
        @DisplayName("Ability loss wears off at end of turn")
        void abilityLossWearsOff() {
            Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

            castTrickster(player2, "Air Elemental");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

            harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

            assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    void alreadyTappedCreatureStillLosesAbilities() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        elemental.tap();

        castTrickster(player2, "Air Elemental");
        resolveAllTriggers();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    void canEnterWithoutAnyLegalEtbTarget() {
        harness.castFromHand(player1, new MerfolkTrickster(), "{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merfolk Trickster");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new MerfolkTrickster());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        castTrickster(player2, "Merfolk Trickster");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Merfolk Trickster");
        assertThat(opponent.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLASH)).isFalse();
    }

    @Test
    void removingAbilitiesDoesNotCounterAnAlreadyActivatedAbility() {
        addCreatureReady(player2, new BlindingMage());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, bears.getId());
        castTrickster(player2, "Blinding Mage");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(bears.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @CardUsed({MerfolkTrickster.class, KnightOfGrace.class, KnightOfMalice.class})
    void removesStaticPowerBoostUntilEndOfTurn() {
        harness.addToBattlefield(player1, new KnightOfMalice());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfGrace());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);

        castTrickster(player2, "Knight of Grace");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isFalse();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void abilityStillResolvesAfterTricksterLeavesBattlefield() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castTrickster(player2, "Air Elemental");
        harness.passBothPriorities();
        Permanent trickster = findPermanent(player1, "Merfolk Trickster");
        gd.playerBattlefields.get(player1.getId()).remove(trickster);
        gd.playerGraveyards.get(player1.getId()).add(trickster.getCard());
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    private void castTrickster(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new MerfolkTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
