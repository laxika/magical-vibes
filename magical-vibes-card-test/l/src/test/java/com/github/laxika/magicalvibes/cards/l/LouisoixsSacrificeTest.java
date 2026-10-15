package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhoiraWeatherlightCaptain;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LouisoixsSacrifice.class, ArvadTheCursed.class, LightningBolt.class,
        RodOfRuin.class, JhoiraWeatherlightCaptain.class, Spellbook.class, GrizzlyBears.class})
class LouisoixsSacrificeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a legendary creature to counter a noncreature spell")
    void sacrificesLegendaryCreatureToCounterNoncreatureSpell() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstantWithSacrifice(player1, 0, bolt.getId(), arvad.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arvad the Cursed");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("Pays {2} to counter an activated ability")
    void paysManaToCounterActivatedAbility() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, rod.getId());

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertOnBattlefield(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Counters a triggered ability")
    void countersTriggeredAbility() {
        harness.addToBattlefield(player2, new JhoiraWeatherlightCaptain());
        harness.setHand(player2, List.of(new Spellbook()));
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        StackEntry trigger = harness.getGameData().stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow();
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, trigger.getCard().getId());

        assertThat(harness.getGameData().stack).noneMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    @Test
    @DisplayName("Cannot sacrifice a nonlegendary creature for the additional cost")
    void cannotSacrificeNonlegendaryCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, bolt.getId(), bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Sacrifice is paid while casting, before the counter resolves")
    void sacrificesLegendaryCreatureBeforeResolution() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstantWithSacrifice(player1, 0, bolt.getId(), arvad.getId());

        harness.assertNotOnBattlefield(player1, "Arvad the Cursed");
        harness.assertInGraveyard(player1, "Arvad the Cursed");
        assertThat(gd.stack).hasSize(2);
        harness.assertNotInGraveyard(player2, "Lightning Bolt");

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can pay mana instead of sacrificing an available legendary creature")
    void canPayManaWithLegendaryCreatureAvailable() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        Spellbook spellbook = new Spellbook();
        harness.setHand(player2, List.of(spellbook));
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, spellbook.getId());

        harness.assertOnBattlefield(player1, "Arvad the Cursed");
        harness.assertNotInGraveyard(player1, "Arvad the Cursed");
        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter your own trigger while sacrificing its source")
    void countersOwnTriggerAfterSacrificingItsSource() {
        Permanent jhoira = harness.addToBattlefieldAndReturn(player1, new JhoiraWeatherlightCaptain());
        harness.setLibrary(player1, List.of(new Spellbook()));
        harness.setHand(player1, List.of(new Spellbook(), new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        StackEntry trigger = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow();
        harness.castInstantWithSacrifice(player1, 0, trigger.getTargetableId(), jhoira.getId());
        harness.assertInGraveyard(player1, "Jhoira, Weatherlight Captain");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without paying the additional cost")
    void cannotCastWithoutAdditionalCost() {
        Spellbook spellbook = new Spellbook();
        harness.setHand(player2, List.of(spellbook));
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Louisoix's Sacrifice");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's legendary creature")
    void cannotSacrificeOpponentsLegendaryCreature() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player2, new ArvadTheCursed());
        Spellbook spellbook = new Spellbook();
        harness.setHand(player2, List.of(spellbook));
        harness.setHand(player1, List.of(new LouisoixsSacrifice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, spellbook.getId(), arvad.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Arvad the Cursed");
        harness.assertInHand(player1, "Louisoix's Sacrifice");
        assertThat(gd.stack).hasSize(1);
    }
}
