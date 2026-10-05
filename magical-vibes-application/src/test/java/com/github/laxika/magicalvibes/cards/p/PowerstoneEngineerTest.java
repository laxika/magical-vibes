package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.o.ObliteratingBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerstoneEngineer.class, WrathOfGod.class, Disfigure.class,
        EnergyRefractor.class, ObliteratingBolt.class})
class PowerstoneEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("When Powerstone Engineer dies, it creates a tapped Powerstone token")
    void deathCreatesTappedPowerstone() {
        harness.addToBattlefield(player1, new PowerstoneEngineer());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        Permanent powerstone = powerstones.getFirst();
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    void opponentGetsTokenWhenTheirEngineerDies() {
        harness.addToBattlefield(player2, new PowerstoneEngineer());
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Powerstone Engineer"));
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Powerstone")).hasSize(1);
        assertThat(findPermanents(player2, "Powerstone").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        harness.assertInGraveyard(player2, "Powerstone Engineer");
    }

    @Test
    void exileInsteadOfDeathDoesNotCreateToken() {
        harness.addToBattlefield(player2, new PowerstoneEngineer());
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0,
                harness.getPermanentId(player2, "Powerstone Engineer"));

        harness.assertNotOnBattlefield(player2, "Powerstone Engineer");
        harness.assertNotInGraveyard(player2, "Powerstone Engineer");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void powerstoneManaPaysForArtifactSpell() {
        createPowerstone();
        Permanent powerstone = findPermanents(player1, "Powerstone").getFirst();
        powerstone.setTapped(false);
        harness.activateAbility(player1, 0, null, null);
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Energy Refractor");
    }

    @Test
    void powerstoneManaCannotPayForNonartifactSpell() {
        createPowerstone();
        findPermanents(player1, "Powerstone").getFirst().setTapped(false);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        harness.setHand(player1, List.of(new PowerstoneEngineer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        harness.assertInHand(player1, "Powerstone Engineer");
        harness.assertNotOnBattlefield(player1, "Powerstone Engineer");
    }

    private void createPowerstone() {
        harness.addToBattlefield(player1, new PowerstoneEngineer());
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Powerstone Engineer"));
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }
}
