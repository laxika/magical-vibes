package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BasriTomorrowsChampion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyseersChariot.class, Forest.class, GrizzlyBears.class, ZuranSpellcaster.class, BasriTomorrowsChampion.class})
class SkyseersChariotTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Skyseer's Chariot offers nonland card names")
    void choosesNonlandCardName() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new ZuranSpellcaster());
        harness.setHand(player1, List.of(new SkyseersChariot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Zuran Spellcaster").doesNotContain("Forest");
        harness.handleListChoice(player1, "Zuran Spellcaster");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .extracting(Permanent::getChosenName).isEqualTo("Zuran Spellcaster");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Taxes activated abilities of permanents with the chosen name")
    void taxesActivatedAbilitiesOfChosenName() {
        addReadyChariot(player1, "Zuran Spellcaster");
        addReadySpellcaster(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Crew 2 animates Skyseer's Chariot and taps the crew")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent chariot = addReadyChariot(player1, "Different Card");
        Permanent crew = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chariot)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void taxesControllersOwnAbilities() {
        addReadyChariot(player1, "Zuran Spellcaster");
        addReadySpellcaster(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void multipleChariotsAddTheirTaxes() {
        addReadyChariot(player1, "Zuran Spellcaster");
        addReadyChariot(player1, "Zuran Spellcaster");
        addReadySpellcaster(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    void differentNamesAreNotTaxed() {
        addReadyChariot(player1, "Skyseer's Chariot");
        addReadySpellcaster(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    void taxesCyclingFromHand() {
        addReadyChariot(player1, "Basri, Tomorrow's Champion");
        harness.setHand(player2, List.of(new BasriTomorrowsChampion()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player2, 0, null);
        harness.assertInGraveyard(player2, "Basri, Tomorrow's Champion");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    void namingChariotTaxesItsOwnCrewAbility() {
        Permanent chariot = addReadyChariot(player1, "Skyseer's Chariot");
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BasriTomorrowsChampion());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        assertThat(crew.isTapped()).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, chariot)).isTrue();
    }

    @Test
    void taxEndsWhenChariotLeavesBattlefield() {
        Permanent chariot = addReadyChariot(player1, "Zuran Spellcaster");
        addReadySpellcaster(player2);
        gd.playerBattlefields.get(player1.getId()).remove(chariot);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }
    private Permanent addReadyChariot(Player player, String chosenName) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SkyseersChariot());
        permanent.setChosenName(chosenName);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addReadySpellcaster(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ZuranSpellcaster());
        permanent.setSummoningSick(false);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
