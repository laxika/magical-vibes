package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.CardUsedExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
@ExtendWith(CardUsedExtension.class)
@CardUsed({LeylineOfVitality.class, RuneclawBear.class, GiantSpider.class})
class LeylineOfVitalityTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameQueryService gqs;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        // Do NOT call skipMulligan() here — leyline tests need to set hand first
    }

    @Test
    @DisplayName("Leyline in opening hand prompts may ability at game start")
    void leylineInOpeningHandPromptsChoice() {
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting leyline places it on the battlefield from hand")
    void acceptingLeylinePlacesOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Leyline of Vitality");
        harness.assertNotInHand(player1, "Leyline of Vitality");
    }

    @Test
    @DisplayName("Declining leyline keeps it in hand")
    void decliningLeylineKeepsInHand() {
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Leyline of Vitality");
        harness.assertInHand(player1, "Leyline of Vitality");
    }

    @Test
    @DisplayName("Own creatures get +0/+1")
    void buffsOwnCreaturesToughness() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs all own creatures")
    void buffsAllOwnCreatures() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new GiantSpider());

        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(gqs.getEffectivePower(gd, p))
                        .isEqualTo(p.getCard().getPower());
                assertThat(gqs.getEffectiveToughness(gd, p))
                        .isEqualTo(p.getCard().getToughness() + 1);
            }
        }
    }

    @Test
    @DisplayName("Two Leylines give +0/+2")
    void twoLeylinesStack() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player1, new LeylineOfVitality());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus is removed when Leyline of Vitality leaves the battlefield")
    void bonusRemovedWhenLeylineLeaves() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        // Remove Leyline from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Leyline of Vitality"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature entering triggers may prompt, accepting gains 1 life")
    void creatureEnteringTriggersLifeGainAccepted() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        // Resolve creature spell — creature enters, MayEffect trigger goes on stack
        harness.passBothPriorities();
        // Resolve MayEffect from stack -> may prompt
        harness.passBothPriorities();

        // May prompt should be awaiting input
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Accept — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Creature entering triggers may prompt, declining gains no life")
    void creatureEnteringTriggersLifeGainDeclined() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        // Resolve creature spell — creature enters, MayEffect trigger goes on stack
        harness.passBothPriorities();
        // Resolve MayEffect from stack -> may prompt
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger for opponent's creatures entering")
    void doesNotTriggerForOpponentCreatures() {
        harness.skipMulligan();
        // Player1's Leyline on battlefield
        harness.addToBattlefield(player1, new LeylineOfVitality());

        // Player2 casts a creature on their turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new RuneclawBear()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);
        // Resolve creature spell
        harness.passBothPriorities();

        // No may prompt for player1 — Leyline only triggers for its controller's creatures
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Leyline of Vitality"));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Leyline of Vitality can be cast normally for {2}{G}{G}")
    void canBeCastNormally() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Leyline of Vitality");
    }

    @Test
    @DisplayName("Leyline placed from opening hand immediately buffs creatures that enter afterward")
    void leylineFromOpeningHandBuffsCreatures() {
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("Leyline animated by Opalescence receives its own toughness bonus")
    void animatedLeylineBuffsItself() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new Opalescence());
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfVitality());

        assertThat(gqs.isCreature(gd, leyline)).isTrue();
        assertThat(gqs.getEffectivePower(gd, leyline)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leyline)).isEqualTo(5);
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("Leyline entering as a creature triggers its own optional life gain")
    void animatedLeylineTriggersForItsOwnEntry() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new Opalescence());
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Leyline of Vitality"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Multiple opening-hand Leylines can be accepted or declined independently")
    void openingHandChoicesAreIndependent() {
        harness.setHand(player1, List.of(new LeylineOfVitality(), new LeylineOfVitality()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof LeylineOfVitality).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c instanceof LeylineOfVitality).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Each Leyline offers a separate optional life gain for one creature")
    void twoLeylinesOfferIndependentLifeGainChoices() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain still resolves after its Leyline leaves the battlefield")
    void lifeGainResolvesAfterSourceLeaves() {
        harness.skipMulligan();
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfVitality());
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(leyline);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 1);
    }
}
