package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuardiansOfKoilosTest extends BaseCardTest {

    private void castGuardians() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GuardiansOfKoilos()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

    @Nested
    @DisplayName("ETB may bounce historic")
    class EtbMayBounce {

        @Test
        @DisplayName("ETB triggers may ability prompt when historic permanent exists")
        void etbTriggersMayPrompt() {
            harness.addToBattlefield(player1, new GildedLotus());
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect from stack

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        }

        @Test
        @DisplayName("Accepting may bounces target artifact to hand")
        void acceptingMayBouncesArtifact() {
            harness.addToBattlefield(player1, new GildedLotus());
            UUID lotusId = harness.getPermanentId(player1, "Gilded Lotus");
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
            harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
            harness.handlePermanentChosen(player1, lotusId);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Gilded Lotus"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Gilded Lotus"));
        }

        @Test
        @DisplayName("Accepting may bounces legendary permanent to hand")
        void acceptingMayBouncesLegendary() {
            harness.addToBattlefield(player1, new ArvadTheCursed());
            UUID arvadId = harness.getPermanentId(player1, "Arvad the Cursed");
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
            harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
            harness.handlePermanentChosen(player1, arvadId);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Arvad the Cursed"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Arvad the Cursed"));
        }

        @Test
        @DisplayName("Declining may does not bounce anything")
        void decliningMayDoesNotBounce() {
            harness.addToBattlefield(player1, new GildedLotus());
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
            harness.handleMayAbilityChosen(player1, false); // decline

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Gilded Lotus"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Guardians of Koilos"));
        }

        @Test
        @DisplayName("Guardians of Koilos enters the battlefield after resolution")
        void guardiansEntersBattlefield() {
            harness.addToBattlefield(player1, new GildedLotus());
            UUID lotusId = harness.getPermanentId(player1, "Gilded Lotus");
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, lotusId);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Guardians of Koilos"));
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target opponent's historic permanent — auto-skips when no own historic exists")
        void cannotTargetOpponentHistoric() {
            harness.addToBattlefield(player2, new GildedLotus());
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
            harness.handleMayAbilityChosen(player1, true); // accept -> no valid targets, auto-skips

            // Opponent's Gilded Lotus is still on the battlefield
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Gilded Lotus"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Cannot target itself — 'another' excludes source, auto-skips with no other historics")
        void cannotTargetItself() {
            // No other historic permanents — only Guardians itself
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
            harness.handleMayAbilityChosen(player1, true); // accept -> no valid targets, auto-skips

            // Guardians is still on the battlefield (wasn't bounced)
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Guardians of Koilos"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Can bounce another artifact creature you control")
        void canBounceAnotherArtifactCreature() {
            // Add another Guardians as a second artifact creature
            harness.addToBattlefield(player1, new GuardiansOfKoilos());
            UUID otherGuardiansId = harness.getPermanentId(player1, "Guardians of Koilos");
            castGuardians();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
            harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
            harness.handlePermanentChosen(player1, otherGuardiansId);

            // The first Guardians should be bounced, the newly cast one remains
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Guardians of Koilos"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Guardians of Koilos"));
        }
    }
}
