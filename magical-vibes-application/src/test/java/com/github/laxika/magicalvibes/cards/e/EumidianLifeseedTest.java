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
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EumidianLifeseed.class, AdagiaWindsweptBastion.class, BlastZone.class,
        CascadingCataracts.class, ContestedWarZone.class, DesertedTemple.class, DustBowl.class,
        KavaronMemorialWorld.class, Mutavault.class, NestingGrounds.class, PlazaOfHeroes.class,
        SunkenCitadel.class, SusurSecundiVoidAltar.class, TerrainGenerator.class,
        UthrosTitanicGodcore.class})
class EumidianLifeseedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB drafts one of three cards from its land spellbook and puts it tapped")
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

        Permanent chosen = findPermanent(player1, chosenName);
        assertThat(chosen.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(chosen.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds any chosen color to the land-ability-only pool")
    void addsRestrictedAnyColorMana() {
        Permanent lifeseed = harness.addToBattlefieldAndReturn(player1, new EumidianLifeseed());
        lifeseed.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyMana(ManaColor.RED)).isEqualTo(1);
    }
}
