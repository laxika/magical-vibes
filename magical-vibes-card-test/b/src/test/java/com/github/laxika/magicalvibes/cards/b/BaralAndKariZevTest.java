package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CorruptedConviction;
import com.github.laxika.magicalvibes.cards.e.EssenceScatter;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.StokeTheFlames;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaralAndKariZev.class, DarkRitual.class, GrizzlyBears.class, LightningStrike.class,
        CorruptedConviction.class, EssenceScatter.class, StokeTheFlames.class})
class BaralAndKariZevTest extends BaseCardTest {

    @Test
    @DisplayName("The first instant or sorcery offers a cheaper spell sharing its card type")
    void firstInstantOrSorceryOffersCheaperSharedTypeSpell() {
        setupBaral();
        LightningStrike lightningStrike = new LightningStrike();
        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player1, List.of(lightningStrike, darkRitual));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(darkRitual.getId()));
    }

    @Test
    @DisplayName("Declining every eligible spell creates First Mate Ragavan")
    void decliningEligibleSpellCreatesRagavan() {
        setupBaral();
        LightningStrike lightningStrike = new LightningStrike();
        harness.setHand(player1, List.of(lightningStrike, new DarkRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        Permanent ragavan = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("First Mate Ragavan"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, ragavan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ragavan)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ragavan, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("No qualifying spell creates First Mate Ragavan without a choice")
    void noQualifyingSpellCreatesRagavan() {
        setupBaral();
        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("First Mate Ragavan"));
    }

    @Test
    @DisplayName("Only the first instant or sorcery each turn triggers")
    void onlyFirstInstantOrSorceryTriggers() {
        setupBaral();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void setupBaral() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new BaralAndKariZev());
    }

    @Test
    @DisplayName("A spell cast before Baral entered counts as the first one")
    void spellCastBeforeBaralEnteredStillCountsAsFirstSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.addToBattlefield(player1, new BaralAndKariZev());
        harness.setHand(player1, List.of(new LightningStrike(), new DarkRitual()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "First Mate Ragavan");
    }

    @Test
    @DisplayName("Declining one offer and casting another creates no token")
    void decliningOneCandidateAndCastingAnotherDoesNotCreateToken() {
        setupBaral();
        harness.setHand(player1, List.of(new LightningStrike(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).filteredOn(card -> card instanceof DarkRitual)
                .hasSize(1);
        harness.assertNotOnBattlefield(player1, "First Mate Ragavan");
    }

    @Test
    @DisplayName("The free spell still requires its sacrifice cost")
    void freeSpellStillRequiresCreatureSacrificeAdditionalCost() {
        setupBaral();
        harness.setHand(player1, List.of(new LightningStrike(), new CorruptedConviction()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Baral and Kari Zev"));
        }
        harness.assertNotOnBattlefield(player1, "Baral and Kari Zev");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof CorruptedConviction);
    }

    @Test
    @DisplayName("An accepted spell with no legal targets stays in hand and the token is created")
    void spellWithNoLegalTargetsStaysInHandAndCreatesToken() {
        setupBaral();
        EssenceScatter essenceScatter = new EssenceScatter();
        harness.setHand(player1, List.of(new StokeTheFlames(), essenceScatter));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(essenceScatter);
        harness.assertOnBattlefield(player1, "First Mate Ragavan");
    }

    @Test
    @CardUsed(BonecrusherGiant.class)
    @DisplayName("Offers an Adventure whose spell has lesser mana value and shares a card type")
    void offersAnAdventureThatFits() {
        setupBaral();
        BonecrusherGiant adventurer = new BonecrusherGiant();
        harness.setHand(player1, List.of(new StokeTheFlames(), adventurer));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice offer =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(offer).isNotNull();
        assertThat(offer.description()).contains("Stomp");
        harness.handleMayAbilityChosen(player1, false);
        harness.assertOnBattlefield(player1, "First Mate Ragavan");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(adventurer);
    }

    @Test
    @DisplayName("A spell of another card type is not offered and the token is created")
    void spellOfAnotherCardTypeIsNotOffered() {
        setupBaral();
        harness.setHand(player1, List.of(new StokeTheFlames(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "First Mate Ragavan");
    }
}
