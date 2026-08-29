package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Redshift, Rocketeer Chief")
class RedshiftRocketeerChiefTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds ability-only mana equal to current power")
    void tapAddsAbilityOnlyManaEqualToPower() {
        Permanent redshift = addReadyRedshift();
        redshift.setPowerModifier(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability-only mana cannot pay for a spell")
    void abilityOnlyManaCannotPayForSpell() {
        addReadyRedshift();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exhaust puts any number of permanent cards from hand onto the battlefield")
    void exhaustPutsAnyNumberOfPermanentsFromHandOntoBattlefield() {
        addReadyRedshift();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new LightningBolt();
        harness.setHand(player1, List.of(creature, land, spell));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .contains(creature, land);
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addReadyRedshift();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addReadyRedshift() {
        Permanent redshift = harness.addToBattlefieldAndReturn(player1, new RedshiftRocketeerChief());
        redshift.setSummoningSick(false);
        return redshift;
    }
}
