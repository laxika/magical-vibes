package com.github.laxika.magicalvibes.cards.m;
import java.util.Set;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Metamorphosis.class, GrizzlyBears.class, RagingGoblin.class})
class MetamorphosisTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and adds one mana plus its mana value for creature spells")
    void sacrificesCreatureAndAddsManaBasedOnManaValue() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Metamorphosis()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature-spell-only mana can cast a creature spell")
    void creatureSpellOnlyManaCanCastCreatureSpell() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        harness.setHand(player1, List.of(new Metamorphosis(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreature() {
        harness.setHand(player1, List.of(new Metamorphosis()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot spend the generated mana to cast a noncreature spell")
    void generatedManaCannotCastNoncreatureSpell() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Metamorphosis(), new Metamorphosis()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0,
                harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Metamorphosis");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature as the additional cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Metamorphosis()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Metamorphosis");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a manifested creature adds only one mana")
    void faceDownCreatureHasZeroManaValue() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        sacrifice.getFaceDownCardTypes().add(CardType.CREATURE);
        harness.setHand(player1, List.of(new Metamorphosis()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
    }
}
