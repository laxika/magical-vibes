package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.e.ElendaSaintOfDusk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OdricLunarchMarshal.class, GrizzlyBears.class, CloudSprite.class})
class OdricLunarchMarshalTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Shares flying from one creature to all creatures you control at beginning of combat")
    void sharesFlyingAtBeginningOfCombat() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new CloudSprite());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, odric, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Shares multiple keywords present among controlled creatures")
    void sharesMultipleKeywords() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new CloudSprite());
        bears.getGrantedKeywords().add(Keyword.VIGILANCE);
        odric.getGrantedKeywords().add(Keyword.LIFELINK);

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, odric, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, odric, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does nothing when no shared keywords are present")
    void doesNothingWithoutKeywords() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(odric.getGrantedKeywords()).isEmpty();
        assertThat(bears.getGrantedKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Does not share opponent creatures' keywords")
    void ignoresOpponentKeywords() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CloudSprite());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, odric, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Triggers during opponent's combat as well")
    void triggersOnOpponentsCombat() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CloudSprite());

        advanceToCombatAndResolve(player2);

        assertThat(gqs.hasKeyword(gd, odric, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new OdricLunarchMarshal());
        harness.addToBattlefield(player1, new CloudSprite());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Shared skulk prevents blocking by higher-power creatures")
    void sharedSkulkBlocksHigherPower() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.getGrantedKeywords().add(Keyword.SKULK);
        harness.addToBattlefield(player1, new OdricLunarchMarshal());

        Permanent highPowerBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        highPowerBlocker.setPowerModifier(2); // 4 power > attacker 2

        advanceToCombatAndResolve(player1);

        Permanent odric = findPermanent(player1, "Odric, Lunarch Marshal");
        assertThat(gqs.hasKeyword(gd, odric, Keyword.SKULK)).isTrue();

        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(skulk)");
    }

    @ParameterizedTest
    @EnumSource(value = Keyword.class, names = {
            "FIRST_STRIKE", "FLYING", "DEATHTOUCH", "DOUBLE_STRIKE", "HASTE", "HEXPROOF",
            "INDESTRUCTIBLE", "LIFELINK", "MENACE", "REACH", "SKULK", "TRAMPLE", "VIGILANCE"
    })
    void sharesEachListedKeyword(Keyword keyword) {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        donor.getGrantedKeywords().add(keyword);

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, odric, keyword)).isTrue();
        assertThat(gqs.hasKeyword(gd, recipient, keyword)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, keyword)).isFalse();
    }

    @Test
    void sharesKeywordGainedAfterTriggering() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        donor.getGrantedKeywords().add(Keyword.FLYING);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, odric, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotShareKeywordLostBeforeResolution() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        donor.getGrantedKeywords().add(Keyword.FLYING);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        donor.getGrantedKeywords().remove(Keyword.FLYING);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, odric, Keyword.FLYING)).isFalse();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGainKeywords() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        harness.addToBattlefield(player1, new CloudSprite());
        advanceToCombatAndResolve(player1);

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, odric, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @CardUsed({ElendaSaintOfDusk.class})
    void sharesHexproofFromInstantsWithoutGrantingUnrestrictedHexproof() {
        Permanent odric = harness.addToBattlefieldAndReturn(player1, new OdricLunarchMarshal());
        Permanent elenda = harness.addToBattlefieldAndReturn(player1, new ElendaSaintOfDusk());
        assertThat(gqs.hasHexproofFromCardType(gd, elenda, CardType.INSTANT, player2.getId())).isTrue();

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasHexproofFromCardType(gd, odric, CardType.INSTANT, player2.getId())).isTrue();
        assertThat(gqs.hasHexproofFromCardType(gd, odric, CardType.SORCERY, player2.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, odric, Keyword.HEXPROOF)).isFalse();
    }
}
