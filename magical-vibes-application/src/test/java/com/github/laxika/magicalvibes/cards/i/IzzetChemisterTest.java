package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IzzetChemister.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class IzzetChemisterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability only targets an instant or sorcery card in your graveyard")
    void firstAbilityFiltersGraveyardTargets() {
        Permanent chemister = addReadyChemister();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.RED, 1);
        preparePriority();

        int chemisterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chemister);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, chemisterIndex, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The sacrifice ability casts any number of tracked cards for free")
    void sacrificeAbilityCastsAnyNumberOfExiledCardsForFree() {
        Permanent chemister = addReadyChemister();
        LightningBolt bolt = new LightningBolt();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(bolt, shock));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        preparePriority();

        int chemisterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chemister);
        harness.activateAbility(player1, chemisterIndex, 0, null, bolt.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        chemister.untap();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, chemisterIndex, 0, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        chemister.untap();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, chemisterIndex, 1, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, bolt.getId(), player2.getId());
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Izzet Chemister");
        assertThat(gd.exiledCards).noneMatch(entry ->
                entry.card().getId().equals(bolt.getId()) || entry.card().getId().equals(shock.getId()));
    }

    private Permanent addReadyChemister() {
        Permanent chemister = new Permanent(new IzzetChemister());
        chemister.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(chemister);
        return chemister;
    }

    private void preparePriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
