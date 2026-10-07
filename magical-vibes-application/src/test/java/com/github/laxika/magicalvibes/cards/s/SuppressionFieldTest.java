package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Brainspoil;
import com.github.laxika.magicalvibes.cards.b.BorosGuildmage;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.f.FiremaneAngel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningStorm;
import com.github.laxika.magicalvibes.cards.t.TorrentElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuppressionField.class, BorosGuildmage.class, BorosSignet.class, Brainspoil.class,
        FiremaneAngel.class, Forest.class, LightningStorm.class, TorrentElemental.class})
class SuppressionFieldTest extends BaseCardTest {

    @Test
    void multipleFieldsAddTheirTaxes() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new BorosGuildmage());
        harness.addToBattlefield(player1, new SuppressionField());
        harness.addToBattlefield(player2, new SuppressionField());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, guildmage.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void transmuteSucceedsWhenTheTaxIsPaid() {
        harness.addToBattlefield(player2, new SuppressionField());
        harness.setHand(player1, List.of(new Brainspoil()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Brainspoil");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void graveyardAbilitySucceedsWhenTheTaxIsPaid() {
        harness.addToBattlefield(player2, new SuppressionField());
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Firemane Angel");
        harness.assertNotInGraveyard(player1, "Firemane Angel");
    }

    @Test
    void taxesAnAbilityOfASpellOnTheStack() {
        harness.addToBattlefield(player1, new SuppressionField());
        LightningStorm storm = new LightningStorm();
        harness.setHand(player1, List.of(storm));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateStackAbility(player2, storm.getId(), 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void taxesAnAbilityFromExile() {
        harness.addToBattlefield(player2, new SuppressionField());
        TorrentElemental elemental = new TorrentElemental();
        harness.setExile(player1, List.of(elemental));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateExileAbility(player1, elemental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elemental);
    }

    @Test
    @DisplayName("Taxes non-mana activated abilities by two generic mana")
    void taxesNonManaActivatedAbilities() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new BorosGuildmage());
        harness.addToBattlefield(player2, new SuppressionField());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, guildmage.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not tax mana abilities")
    void doesNotTaxManaAbilities() {
        harness.addToBattlefield(player1, new BorosSignet());
        harness.addToBattlefield(player2, new SuppressionField());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taxes the controller's own non-mana activated abilities")
    void taxesControllersOwnNonManaActivatedAbilities() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new BorosGuildmage());
        harness.addToBattlefield(player1, new SuppressionField());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, guildmage.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Taxes non-mana activated abilities from hand")
    void taxesNonManaHandAbilities() {
        harness.addToBattlefield(player1, new SuppressionField());
        harness.setHand(player1, List.of(new Brainspoil()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Taxes non-mana activated abilities from the graveyard")
    void taxesNonManaGraveyardAbilities() {
        harness.addToBattlefield(player1, new SuppressionField());
        harness.setGraveyard(player1, List.of(new FiremaneAngel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
