package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.StoneGolem;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.AzusaLostButSeeking;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.h.HelmOfTheHost;
import com.github.laxika.magicalvibes.cards.r.RaffCapashenShipsMage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuldrothaTheGravetide.class, Forest.class, GrizzlyBears.class, DarksteelRelic.class,
        Levitation.class, LightningBolt.class, StoneGolem.class, AzusaLostButSeeking.class,
        DeepFreeze.class, KarnScionOfUrza.class, RaffCapashenShipsMage.class, HelmOfTheHost.class})
class MuldrothaTheGravetideTest extends BaseCardTest {

    @Test
    @DisplayName("Muldrotha can be cast as a creature")
    void canBeCast() {
        harness.setHand(player1, List.of(new MuldrothaTheGravetide()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Muldrotha, the Gravetide");
    }

    @Test
    @DisplayName("Can play a land from graveyard with Muldrotha on battlefield")
    void canPlayLandFromGraveyard() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Can cast a creature from graveyard with Muldrotha on battlefield")
    void canCastCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast an artifact from graveyard with Muldrotha on battlefield")
    void canCastArtifactFromGraveyard() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new DarksteelRelic()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("Can cast an enchantment from graveyard with Muldrotha on battlefield")
    void canCastEnchantmentFromGraveyard() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new Levitation()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Levitation");
    }

    @Test
    @DisplayName("Can cast one creature AND one artifact from graveyard in the same turn")
    void canCastDifferentTypesInSameTurn() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DarksteelRelic()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast creature from graveyard
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        // Cast artifact from graveyard (different type, should work)
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("Cannot cast two creatures from graveyard in the same turn")
    void cannotCastTwoCreaturesInSameTurn() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast first creature
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        // Second creature should not be castable
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast two artifacts from graveyard in the same turn")
    void cannotCastTwoArtifactsInSameTurn() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        DarksteelRelic relic1 = new DarksteelRelic();
        DarksteelRelic relic2 = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(relic1, relic2));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast first artifact
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        // Second artifact should not be castable
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Player can choose CREATURE type for artifact creature, leaving ARTIFACT slot open")
    void chooseCreatureTypeForArtifactCreature() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        // StoneGolem is an Artifact Creature — player chooses CREATURE, so ARTIFACT slot stays open
        harness.setGraveyard(player1, List.of(new StoneGolem(), new DarksteelRelic()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast Stone Golem choosing CREATURE slot
        harness.castFromGraveyard(player1, 0, CardType.CREATURE);
        harness.passBothPriorities();

        // DarksteelRelic is a pure artifact — ARTIFACT slot is still available
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stone Golem");
        harness.assertOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("Player can choose ARTIFACT type for artifact creature, leaving CREATURE slot open")
    void chooseArtifactTypeForArtifactCreature() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new StoneGolem(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast Stone Golem choosing ARTIFACT slot
        harness.castFromGraveyard(player1, 0, CardType.ARTIFACT);
        harness.passBothPriorities();

        // GrizzlyBears is a pure creature — CREATURE slot is still available
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stone Golem");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing ARTIFACT for artifact creature blocks casting another artifact")
    void chooseArtifactBlocksArtifactSlot() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new StoneGolem(), new DarksteelRelic()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast Stone Golem choosing ARTIFACT slot
        harness.castFromGraveyard(player1, 0, CardType.ARTIFACT);
        harness.passBothPriorities();

        // DarksteelRelic (pure artifact) — ARTIFACT slot is used
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose an invalid type for the card")
    void cannotChooseInvalidType() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // GrizzlyBears is only a creature — choosing ARTIFACT should fail
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, CardType.ARTIFACT))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("New Muldrotha entering resets graveyard cast type tracking")
    void newMuldrothaResetsTracking() {
        MuldrothaTheGravetide muldrothaA = new MuldrothaTheGravetide();
        harness.addToBattlefield(player1, muldrothaA);
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast first creature using Muldrotha A
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        // Remove Muldrotha A (it dies)
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Muldrotha, the Gravetide"));

        // New Muldrotha B enters — fresh tracking
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Second creature should now be castable (new Muldrotha, fresh slots)
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast an instant from graveyard via Muldrotha")
    void cannotCastInstantFromGraveyard() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast permanent from graveyard without Muldrotha on battlefield")
    void cannotCastWithoutMuldrotha() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Muldrotha disables graveyard casting")
    void removingMuldrothaDisablesAbility() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Remove Muldrotha
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Muldrotha, the Gravetide"));

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Muldrotha only allows its controller to cast from graveyard")
    void onlyAffectsController() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast permanent from graveyard during opponent's turn")
    void cannotCastDuringOpponentTurn() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast permanent from graveyard during combat")
    void cannotCastDuringCombat() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard casting type limits reset on new turn")
    void limitsResetOnNewTurn() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast first creature
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can play a land AND cast a creature from graveyard in the same turn")
    void canPlayLandAndCastCreatureInSameTurn() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play land from graveyard
        harness.playGraveyardLand(player1, 0);

        // Cast creature from graveyard
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast permanent from graveyard during postcombat main phase")
    void worksInPostcombatMain() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new DarksteelRelic()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    void cannotPlayTwoGraveyardLandsEvenWithAdditionalLandPlays() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.addToBattlefield(player1, new AzusaLostButSeeking());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotCastFlashPermanentDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new RaffCapashenShipsMage()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Raff Capashen, Ship's Mage");
    }

    @Test
    void canCastFlashPermanentDuringOwnCombat() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new RaffCapashenShipsMage()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raff Capashen, Ship's Mage");
        harness.assertNotInGraveyard(player1, "Raff Capashen, Ship's Mage");
    }

    @Test
    void losingAbilitiesDisablesGraveyardSpellCasting() {
        var muldrotha = harness.addToBattlefieldAndReturn(player1, new MuldrothaTheGravetide());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.setGraveyard(player1, List.of(new DarksteelRelic()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, muldrotha.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    void canCastOnlyOnePlaneswalkerFromGraveyardPerTurn() {
        harness.addToBattlefield(player1, new MuldrothaTheGravetide());
        harness.setGraveyard(player1, List.of(new KarnScionOfUrza(), new KarnScionOfUrza()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Karn, Scion of Urza");
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachMuldrothaHasItsOwnCreaturePermission() {
        var muldrotha = harness.addToBattlefieldAndReturn(player1, new MuldrothaTheGravetide());
        var helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheHost());
        helm.setAttachedTo(muldrotha.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Muldrotha, the Gravetide")).isEqualTo(2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
