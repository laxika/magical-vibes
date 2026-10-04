package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.o.OfOneMind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HumbleNaturalist.class, AlmightyBrushwagg.class, OfOneMind.class})
class HumbleNaturalistTest extends BaseCardTest {

    @Test
    void tappingPromptsForAManaColorWithoutUsingTheStack() {
        Permanent naturalist = addReadyNaturalist();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(naturalist.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    void chosenColorProducesCreatureSpellOnlyMana() {
        addReadyNaturalist();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED"})
    void otherColorsProduceExactlyOneCreatureSpellOnlyMana(ManaColor color) {
        addReadyNaturalist();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(color)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(color)).isEqualTo(1);
        assertThat(pool.getTotalAllMana()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureSpellOnlyManaCanCastCreatureSpells() {
        addReadyNaturalist();
        harness.setHand(player1, List.of(new AlmightyBrushwagg()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isZero();
    }

    @Test
    void creatureSpellOnlyManaCannotCastNoncreatureSpells() {
        addReadyNaturalist();
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureSpellOnlyManaCanPayGenericCreatureCosts() {
        addReadyNaturalist();
        harness.setHand(player1, List.of(new HumbleNaturalist()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void creatureSpellOnlyManaCannotPayCreatureActivatedAbilityCosts() {
        addReadyNaturalist();
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isEqualTo(1);
    }

    @Test
    void summoningSickNaturalistCannotActivateTapAbility() {
        Permanent naturalist = harness.addToBattlefieldAndReturn(player1, new HumbleNaturalist());
        naturalist.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(naturalist.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void tappedNaturalistCannotActivateAgain() {
        addReadyNaturalist();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isEqualTo(1);
    }

    private Permanent addReadyNaturalist() {
        return addCreatureReady(player1, new HumbleNaturalist());
    }
}
