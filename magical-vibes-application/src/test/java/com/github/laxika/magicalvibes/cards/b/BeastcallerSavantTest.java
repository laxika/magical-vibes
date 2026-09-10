package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeastcallerSavant.class, LlanowarElves.class, Divination.class})
class BeastcallerSavantTest extends BaseCardTest {

    @Test
    void tappingPromptsForManaColorWithoutUsingTheStack() {
        Permanent savant = addReadySavant();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(savant.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    void chosenColorProducesCreatureSpellOnlyMana() {
        addReadySavant();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void creatureSpellOnlyManaCanCastCreatureSpells() {
        addReadySavant();
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isZero();
    }

    @Test
    void creatureSpellOnlyManaCannotCastNoncreatureSpells() {
        addReadySavant();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySavant() {
        Permanent savant = harness.addToBattlefieldAndReturn(player1, new BeastcallerSavant());
        savant.setSummoningSick(false);
        return savant;
    }
}
