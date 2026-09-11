package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KazaRoilChaser.class, ZuranSpellcaster.class, Divination.class, GrizzlyBears.class})
class KazaRoilChaserTest extends BaseCardTest {

    @Test
    void reducesTheNextInstantOrSorceryByTheNumberOfWizards() {
        addCreatureReady(player1, new KazaRoilChaser());
        addCreatureReady(player1, new ZuranSpellcaster());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
    }

    @Test
    void doesNotReduceCreatureSpells() {
        addCreatureReady(player1, new KazaRoilChaser());
        addCreatureReady(player1, new ZuranSpellcaster());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
