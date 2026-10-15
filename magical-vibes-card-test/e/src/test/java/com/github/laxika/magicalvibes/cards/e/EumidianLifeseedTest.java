package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.b.BlastZone;
import com.github.laxika.magicalvibes.cards.c.CascadingCataracts;
import com.github.laxika.magicalvibes.cards.c.ContestedWarZone;
import com.github.laxika.magicalvibes.cards.d.DesertedTemple;
import com.github.laxika.magicalvibes.cards.d.DustBowl;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.n.NestingGrounds;
import com.github.laxika.magicalvibes.cards.p.PlazaOfHeroes;
import com.github.laxika.magicalvibes.cards.s.SunkenCitadel;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.t.TerrainGenerator;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EumidianLifeseed.class, AdagiaWindsweptBastion.class, BlastZone.class,
        CascadingCataracts.class, ContestedWarZone.class, DesertedTemple.class, DustBowl.class,
        KavaronMemorialWorld.class, Mutavault.class, NestingGrounds.class, PlazaOfHeroes.class,
        SunkenCitadel.class, SusurSecundiVoidAltar.class, TerrainGenerator.class,
        UthrosTitanicGodcore.class})
class EumidianLifeseedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB drafts one of three spellbook cards into hand without putting it onto the battlefield")
    void draftsLandFromSpellbook() {
        harness.setHand(player1, List.of(new EumidianLifeseed()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.context()).isInstanceOf(com.github.laxika.magicalvibes.model.ChoiceContext.ChooseModeChoice.class);
        com.github.laxika.magicalvibes.model.ChoiceContext.ChooseModeChoice context =
                (com.github.laxika.magicalvibes.model.ChoiceContext.ChooseModeChoice) interaction.context();
        assertThat(context.effect().options()).hasSize(3);

        String chosenName = context.effect().options().getFirst().label();
        harness.handleListChoice(player1, chosenName);

        harness.assertInHand(player1, chosenName);
        harness.assertNotOnBattlefield(player1, chosenName);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mana ability adds any chosen color to the land-ability-only pool")
    void addsRestrictedAnyColorMana() {
        Permanent lifeseed = harness.addToBattlefieldAndReturn(player1, new EumidianLifeseed());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyMana(ManaColor.RED)).isEqualTo(1);
        assertThat(lifeseed.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restricted mana pays for a land's activated ability")
    void restrictedManaPaysForLandAbility() {
        harness.addToBattlefield(player1, new EumidianLifeseed());
        Permanent mutavault = harness.addToBattlefieldAndReturn(player1, new Mutavault());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mutavault)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyMana(ManaColor.RED)).isZero();
        assertThat(mutavault.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a spell")
    void restrictedManaCannotPayForSpell() {
        harness.addToBattlefield(player1, new EumidianLifeseed());
        harness.setHand(player1, List.of(new EumidianLifeseed()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        harness.assertInHand(player1, "Eumidian Lifeseed");
    }
}
