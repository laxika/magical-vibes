package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AccorderPaladin;
import com.github.laxika.magicalvibes.cards.a.ArdentRecruit;
import com.github.laxika.magicalvibes.cards.a.AuriokSunchaser;
import com.github.laxika.magicalvibes.cards.b.BarbedBatterfist;
import com.github.laxika.magicalvibes.cards.b.BladeTribeBerserkers;
import com.github.laxika.magicalvibes.cards.b.BladeholdWarWhip;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.d.DragonwingGlider;
import com.github.laxika.magicalvibes.cards.g.GoblinGaveleer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeroOfBladehold;
import com.github.laxika.magicalvibes.cards.h.HeroOfOxidRidge;
import com.github.laxika.magicalvibes.cards.j.JorKadeenThePrevailer;
import com.github.laxika.magicalvibes.cards.m.MirranCrusader;
import com.github.laxika.magicalvibes.cards.o.OxiddaFinisher;
import com.github.laxika.magicalvibes.cards.o.OxiddaScrapmelter;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.SunspearShikari;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeholdCleaver.class, AccorderPaladin.class, ArdentRecruit.class,
        AuriokSunchaser.class, BladeTribeBerserkers.class, GoblinGaveleer.class,
        HeroOfBladehold.class, HeroOfOxidRidge.class, JorKadeenThePrevailer.class,
        MirranCrusader.class, OxiddaScrapmelter.class, SunspearShikari.class,
        OxiddaFinisher.class, BarbedBatterfist.class, BladeholdWarWhip.class,
        DragonwingGlider.class, GrizzlyBears.class, DoomBlade.class, PlanarCleansing.class})
class BladeholdCleaverTest extends BaseCardTest {

    @Test
    void forMirrodinCreatesAndAttachesARebelToken() {
        harness.setHand(player1, List.of(new BladeholdCleaver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cleaver = findPermanent(player1, "Bladehold Cleaver");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(cleaver.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(4);
    }

    @Test
    void equippedCreatureDeathOffersThreeSpellbookCardsAndDraftsTheChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new BladeholdCleaver());
        cleaver.setAttachedTo(creature.getId());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void equipMovesTheBoostWithoutCreatingAnotherRebel() {
        harness.setHand(player1, List.of(new BladeholdCleaver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cleaver = findPermanent(player1, "Bladehold Cleaver");
        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, recruit.getId());
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(recruit.getId());
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void equippedRebelTokenDeathAlsoDraftsACard() {
        harness.setHand(player1, List.of(new BladeholdCleaver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cleaver = findPermanent(player1, "Bladehold Cleaver");
        Permanent rebel = findPermanent(player1, "Rebel");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, rebel.getId());
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).doesNotHaveDuplicates();
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
        assertThat(cleaver.getAttachedTo()).isNull();
        harness.assertNotOnBattlefield(player1, "Rebel");
        harness.assertOnBattlefield(player1, "Bladehold Cleaver");
    }

    @Test
    void draftsWhenEquipmentAndEquippedCreatureAreDestroyedSimultaneously() {
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new BladeholdCleaver());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new ArdentRecruit());
        cleaver.setAttachedTo(recruit.getId());
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bladehold Cleaver");
        harness.assertInGraveyard(player1, "Ardent Recruit");
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
    }
}
