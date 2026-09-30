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
        DragonwingGlider.class, GrizzlyBears.class, DoomBlade.class})
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
        Permanent rebel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Rebel"))
                .findFirst()
                .orElseThrow();

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
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
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
}
