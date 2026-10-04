package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.c.CaligoSkinWitch;
import com.github.laxika.magicalvibes.cards.t.TheFlameOfKeld;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardiansOfKoilos.class, GildedLotus.class, ArvadTheCursed.class,
        TheFlameOfKeld.class, CaligoSkinWitch.class})
class GuardiansOfKoilosTest extends BaseCardTest {

    private void castGuardians() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GuardiansOfKoilos(), "{5}");
    }

    @Test
    void acceptingMayBouncesSaga() {
        UUID sagaId = harness.addToBattlefieldAndReturn(player1, new TheFlameOfKeld()).getId();
        castGuardians();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sagaId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "The Flame of Keld");
        harness.assertInHand(player1, "The Flame of Keld");
    }

    @Test
    void nonhistoricCreatureIsNotALegalTarget() {
        harness.addToBattlefield(player1, new CaligoSkinWitch());
        castGuardians();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Caligo Skin-Witch");
        harness.assertOnBattlefield(player1, "Guardians of Koilos");
    }

    @Test
    void borrowedHistoricPermanentReturnsToOwner() {
        var lotus = harness.addToBattlefieldAndReturn(player1, new GildedLotus());
        gd.stolenCreatures.put(lotus.getId(), player2.getId());
        castGuardians();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, lotus.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Gilded Lotus");
        harness.assertInHand(player2, "Gilded Lotus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void targetChangingControllerBeforeResolutionIsNotReturned() {
        var lotus = harness.addToBattlefieldAndReturn(player1, new GildedLotus());
        castGuardians();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, lotus.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lotus);
        gd.playerBattlefields.get(player2.getId()).add(lotus);
        gd.stolenCreatures.put(lotus.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Gilded Lotus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void targetLeavingBeforeResolutionDoesNotPromptForMay() {
        var lotus = harness.addToBattlefieldAndReturn(player1, new GildedLotus());
        castGuardians();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, lotus.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lotus);
        gd.playerGraveyards.get(player1.getId()).add(lotus.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Gilded Lotus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Nested
    @DisplayName("ETB may bounce historic")
    @CardUsed({GuardiansOfKoilos.class, GildedLotus.class, ArvadTheCursed.class})
    class EtbMayBounce {

        @Test
        @DisplayName("ETB triggers may ability prompt when historic permanent exists")
        void etbTriggersMayPrompt() {
            harness.addToBattlefield(player1, new GildedLotus());
            castGuardians();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gilded Lotus"));
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }

        @Test
        @DisplayName("Accepting may bounces target artifact to hand")
        void acceptingMayBouncesArtifact() {
            UUID lotusId = harness.addToBattlefieldAndReturn(player1, new GildedLotus()).getId();
            castGuardians();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, lotusId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Gilded Lotus");
            harness.assertInHand(player1, "Gilded Lotus");
        }

        @Test
        @DisplayName("Accepting may bounces legendary permanent to hand")
        void acceptingMayBouncesLegendary() {
            UUID arvadId = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed()).getId();
            castGuardians();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, arvadId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Arvad the Cursed");
            harness.assertInHand(player1, "Arvad the Cursed");
        }

        @Test
        @DisplayName("Declining may does not bounce anything")
        void decliningMayDoesNotBounce() {
            harness.addToBattlefield(player1, new GildedLotus());
            castGuardians();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gilded Lotus"));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Gilded Lotus");
            harness.assertOnBattlefield(player1, "Guardians of Koilos");
        }

        @Test
        @DisplayName("Guardians of Koilos enters the battlefield after resolution")
        void guardiansEntersBattlefield() {
            UUID lotusId = harness.addToBattlefieldAndReturn(player1, new GildedLotus()).getId();
            castGuardians();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, lotusId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Guardians of Koilos");
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({GuardiansOfKoilos.class, GildedLotus.class})
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target opponent's historic permanent — may never triggers with no own historic")
        void cannotTargetOpponentHistoric() {
            // The bounce targets "another target historic permanent you control". An opponent's
            // Gilded Lotus is not a legal target, so the targeted "may" ETB has no legal target and
            // is removed from the stack — the controller is never prompted.
            harness.addToBattlefield(player2, new GildedLotus());
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell -> enters battlefield

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            // Opponent's Gilded Lotus is still on the battlefield
            harness.assertOnBattlefield(player2, "Gilded Lotus");
        }

        @Test
        @DisplayName("Cannot target itself — 'another' excludes source, may never triggers with no other historics")
        void cannotTargetItself() {
            // No other historic permanents — only Guardians itself, which "another" excludes. With
            // no legal target the "may" ETB is removed from the stack, so no prompt appears.
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell -> enters battlefield

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            // Guardians is still on the battlefield (wasn't bounced)
            harness.assertOnBattlefield(player1, "Guardians of Koilos");
        }

        @Test
        @DisplayName("Can bounce another artifact creature you control")
        void canBounceAnotherArtifactCreature() {
            // Add another Guardians as a second artifact creature
            UUID otherGuardiansId = harness.addToBattlefieldAndReturn(player1, new GuardiansOfKoilos()).getId();
            castGuardians();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, otherGuardiansId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            // The first Guardians should be bounced, the newly cast one remains
            harness.assertInHand(player1, "Guardians of Koilos");
            harness.assertOnBattlefield(player1, "Guardians of Koilos");
        }
    }
}
