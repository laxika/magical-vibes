package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CacklingFlames;
import com.github.laxika.magicalvibes.cards.c.ChanceEncounter;
import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OddsEnds.class, CacklingFlames.class, EdgarKingOfFigaro.class, MistralCharger.class,
        ChanceEncounter.class})
class OddsEndsTest extends BaseCardTest {

    @Test
    @DisplayName("Odds counters a targeted instant or sorcery when the flip comes up heads")
    void oddsCountersTargetedSpellOnHeads() {
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        CacklingFlames cacklingFlames = new CacklingFlames();
        harness.setHand(player2, List.of(cacklingFlames));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player2.getId());

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, 0, cacklingFlames.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cackling Flames");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Odds copies the spell when the flip comes up tails")
    void oddsCopiesTargetedSpellOnTails() {
        CacklingFlames cacklingFlames = new CacklingFlames();
        harness.setHand(player2, List.of(cacklingFlames));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player2.getId());

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, 0, cacklingFlames.getId());
        harness.passBothPriorities();

        if (gameLogContains("wins the coin flip for Odds")) {
            harness.assertInGraveyard(player2, "Cackling Flames");
            harness.assertLife(player2, player2LifeBefore);
        } else {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, player1.getId());
            resolveAllTriggers();

            harness.assertLife(player1, player1LifeBefore - 5);
            harness.assertLife(player2, player2LifeBefore - 5);
        }
    }

    @Test
    @DisplayName("Odds cannot target a player")
    void oddsCannotTargetPlayer() {
        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ends cannot target a spell")
    void endsCannotTargetSpell() {
        CacklingFlames cacklingFlames = new CacklingFlames();
        harness.setHand(player2, List.of(cacklingFlames));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player2.getId());

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, cacklingFlames.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ends sacrifices two attacking creatures chosen by the targeted player")
    void endsSacrificesTwoAttackingCreatures() {
        Permanent first = addCreatureReady(player2, new MistralCharger());
        Permanent second = addCreatureReady(player2, new MistralCharger());
        Permanent third = addCreatureReady(player2, new MistralCharger());
        Permanent nonAttacker = addCreatureReady(player2, new MistralCharger());

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        declareAttackers(player2, List.of(0, 1, 2));
        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getId())
                .containsExactly(third.getId(), nonAttacker.getId());
    }

    @Test
    @DisplayName("Ends sacrifices all available attackers when fewer than two are attacking")
    void endsSacrificesAllAvailableAttackers() {
        Permanent attacker = addCreatureReady(player2, new MistralCharger());
        Permanent nonAttacker = addCreatureReady(player2, new MistralCharger());

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        declareAttackers(player2, List.of(0));
        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getId())
                .containsExactly(nonAttacker.getId())
                .doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Odds cannot target a creature spell")
    void oddsCannotTargetCreatureSpell() {
        MistralCharger charger = new MistralCharger();
        harness.setHand(player2, List.of(charger));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, charger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ends can target its controller and sacrifices exactly two attackers")
    void endsCanSacrificeControllersAttackers() {
        Permanent first = addCreatureReady(player1, new MistralCharger());
        Permanent second = addCreatureReady(player1, new MistralCharger());
        Permanent nonAttacker = addCreatureReady(player1, new MistralCharger());
        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        declareAttackers(player1, List.of(0, 1));

        harness.castInstant(player1, 0, 1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(nonAttacker.getId())
                .doesNotContain(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof MistralCharger).hasSize(2);
    }

    @Test
    @DisplayName("Ends leaves nonattacking creatures alone when there are no attackers")
    void endsDoesNothingWithoutAttackers() {
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).containsExactly(creature.getId());
        harness.assertNotInGraveyard(player2, "Mistral Charger");
        harness.assertInGraveyard(player1, "Odds // Ends");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Odds heads and tails do not count as winning or losing a coin flip")
    void oddsDoesNotTriggerChanceEncounter() {
        Permanent encounter = harness.addToBattlefieldAndReturn(player1, new ChanceEncounter());
        harness.setLife(player2, 1000);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        for (int attempt = 0; attempt < 32; attempt++) {
            CacklingFlames flames = new CacklingFlames();
            harness.setHand(player2, List.of(flames));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 3);
            harness.castInstant(player2, 0, player2.getId());
            harness.setHand(player1, List.of(new OddsEnds()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, 0, flames.getId());
            harness.passBothPriorities();

            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, false);
            }
            resolveAllTriggers();

            assertThat(encounter.getCounterCount(CounterType.LUCK)).isZero();
        }
    }
}
