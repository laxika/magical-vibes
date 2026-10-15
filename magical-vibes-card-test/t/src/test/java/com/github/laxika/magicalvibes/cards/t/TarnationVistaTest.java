package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LootTheKeyToEverything;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TarnationVista.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class, SuntailHawk.class, HillGiant.class, LootTheKeyToEverything.class})
class TarnationVistaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and asks for a color")
    void entersTappedAndChoosesColor() {
        harness.setHand(player1, List.of(new TarnationVista()));

        harness.playLand(player1, 0);

        Permanent vista = findPermanent(player1, "Tarnation Vista");
        assertThat(vista.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "BLUE");
        assertThat(vista.getChosenColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.BLUE);
    }

    @Test
    @DisplayName("First ability adds mana of the chosen color")
    void firstAbilityAddsChosenColorMana() {
        Permanent vista = addReadyVista();
        vista.setChosenColor(com.github.laxika.magicalvibes.model.CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(vista.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability adds one mana for each distinct color among controlled monocolored permanents")
    void secondAbilityAddsManaForMonocoloredPermanentColors() {
        Permanent vista = addReadyVista();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(vista.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The color selected when playing the land is used by its mana ability")
    void manaAbilityUsesColorChosenOnEntry() {
        harness.setHand(player1, List.of(new TarnationVista()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "BLACK");
        Permanent vista = findPermanent(player1, "Tarnation Vista");
        vista.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(vista.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Duplicate monocolored permanents produce only one mana of their color")
    void duplicateColorsProduceOnlyOneMana() {
        addReadyVista();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multicolored permanents do not contribute colors")
    void ignoresMulticoloredPermanents() {
        addReadyVista();
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With only colorless permanents, the second ability spends its cost and produces no mana")
    void noMonocoloredPermanentsProducesNoMana() {
        Permanent vista = addReadyVista();
        vista.setChosenColor(com.github.laxika.magicalvibes.model.CardColor.RED);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(vista.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyVista() {
        Permanent vista = harness.addToBattlefieldAndReturn(player1, new TarnationVista());
        vista.setSummoningSick(false);
        return vista;
    }
}
