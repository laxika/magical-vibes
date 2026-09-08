package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyChariot(Player player, String chosenName) {
        SkyseersChariot card = new SkyseersChariot();
        Permanent permanent = new Permanent(card);
        permanent.setChosenName(chosenName);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadySpellcaster(Player player) {
        Permanent permanent = new Permanent(new ZuranSpellcaster());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
