package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlhammarretsArchive;
import com.github.laxika.magicalvibes.cards.s.SigilOfTheEmptyThrone;
import com.github.laxika.magicalvibes.cards.c.ClericOfTheForwardOrder;
import com.github.laxika.magicalvibes.cards.b.BondedConstruct;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CausticCaterpillar.class, BondedConstruct.class, SigilOfTheEmptyThrone.class, ClericOfTheForwardOrder.class, AlhammarretsArchive.class})
class CausticCaterpillarTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices the Caterpillar and destroys target artifact")
    void destroysTargetArtifact() {
        addReadyCaterpillar(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent target = addArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Caustic Caterpillar");
        harness.assertInGraveyard(player1, "Caustic Caterpillar");
        harness.assertNotOnBattlefield(player2, "Bonded Construct");
        harness.assertInGraveyard(player2, "Bonded Construct");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyCaterpillar(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent target = addEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sigil of the Empty Throne");
        harness.assertInGraveyard(player2, "Sigil of the Empty Throne");
    }

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new CausticCaterpillar());
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent target = addEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyCaterpillar(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent creature = addCreatureReady(player2, new ClericOfTheForwardOrder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without mana")
    void cannotActivateWithoutMana() {
        addReadyCaterpillar(player1);
        Permanent target = addEnchantment(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and can destroy your own artifact creature")
    void sacrificesAsCostAndDestroysOwnArtifactCreature() {
        harness.addToBattlefield(player1, new CausticCaterpillar());
        Permanent target = addArtifact(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Caustic Caterpillar");
        harness.assertNotOnBattlefield(player1, "Caustic Caterpillar");
        harness.assertOnBattlefield(player1, "Bonded Construct");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bonded Construct");
        harness.assertNotOnBattlefield(player1, "Bonded Construct");
    }

    @Test
    @DisplayName("Activation requires green mana")
    void cannotPayWithOnlyColorlessMana() {
        addReadyCaterpillar(player1);
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Caustic Caterpillar");
        harness.assertNotInGraveyard(player1, "Caustic Caterpillar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires the generic mana in addition to green")
    void cannotPayWithOnlyOneGreenMana() {
        addReadyCaterpillar(player1);
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Caustic Caterpillar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An illegal target does not sacrifice the Caterpillar")
    void illegalTargetDoesNotPaySacrificeCost() {
        addReadyCaterpillar(player1);
        Permanent target = addCreatureReady(player2, new ClericOfTheForwardOrder());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Caustic Caterpillar");
        harness.assertNotInGraveyard(player1, "Caustic Caterpillar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target destroyed in response leaves both sacrifice costs paid")
    void targetDestroyedInResponseDoesNotRefundSacrifices() {
        addReadyCaterpillar(player1);
        addReadyCaterpillar(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlhammarretsArchive());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Caustic Caterpillar");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Caustic Caterpillar"))
                .hasSize(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alhammarret's Archive");
        harness.assertInGraveyard(player2, "Alhammarret's Archive");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Caustic Caterpillar"))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Alhammarret's Archive"))
                .hasSize(1);
    }

    private Permanent addReadyCaterpillar(Player player) {
        return addCreatureReady(player, new CausticCaterpillar());
    }

    private Permanent addEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SigilOfTheEmptyThrone());
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BondedConstruct());
    }
}
