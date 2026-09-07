package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OltecMatterweaver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldwalkerHelm.class, OltecMatterweaver.class, GrizzlyBears.class})
class WorldwalkerHelmTest extends BaseCardTest {

    private static final String GNOME_MODE = "Create a 1/1 colorless Gnome artifact creature token";

    @Test
    void addsMapTokenWhenAnArtifactTokenIsCreated() {
        addHelmAndMatterweaver();

        castCreatureAndChooseGnome();

        assertThat(findPermanents(player1, "Gnome")).hasSize(1);
        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    void addsMapTokenWhenCopyingAnArtifactToken() {
        addHelmAndMatterweaver();
        castCreatureAndChooseGnome();
        Permanent gnome = findPermanent(player1, "Gnome");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, gnome.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gnome")).hasSize(2);
        assertThat(findPermanents(player1, "Map")).hasSize(2);
    }

    private void addHelmAndMatterweaver() {
        harness.addToBattlefield(player1, new WorldwalkerHelm());
        addCreatureReady(player1, new OltecMatterweaver());
    }

    private void castCreatureAndChooseGnome() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, GNOME_MODE);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
