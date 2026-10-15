package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrimsonMage;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Pillage;
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

@CardUsed({IzzetChemister.class, CrimsonMage.class, LightningBolt.class, Pillage.class})
class IzzetChemisterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability only targets an instant or sorcery card in your graveyard")
    void firstAbilityFiltersGraveyardTargets() {
        Permanent chemister = addReadyChemister();
        Card creature = new CrimsonMage();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.RED, 1);
        preparePriority();

        int chemisterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chemister);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, chemisterIndex, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The sacrifice ability offers casting while it is resolving")
    void sacrificeAbilityOffersCastingDuringResolution() {
        Permanent chemister = addReadyChemister();
        LightningBolt bolt = new LightningBolt();
        LightningBolt secondBolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt, secondBolt));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        preparePriority();

        int chemisterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chemister);
        harness.activateAbility(player1, chemisterIndex, 0, null, bolt.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        chemister.untap();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, chemisterIndex, 0, null, secondBolt.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        chemister.untap();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, chemisterIndex, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Izzet Chemister");
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bolt, secondBolt);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("The first ability exiles and tracks a sorcery and pays its tap cost")
    void firstAbilityExilesSorceryAndTapsSource() {
        Permanent chemister = addReadyChemister();
        Pillage pillage = new Pillage();
        harness.setGraveyard(player1, List.of(pillage));
        harness.addMana(player1, ManaColor.RED, 1);
        preparePriority();

        harness.activateAbility(player1, 0, 0, null, pillage.getId(), Zone.GRAVEYARD);
        assertThat(chemister.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pillage);
        assertThat(gd.getCardsExiledByPermanent(chemister.getId())).containsExactly(pillage);
    }

    @Test
    @DisplayName("The first ability cannot target an opponent's graveyard")
    void firstAbilityRejectsOpponentsGraveyard() {
        addReadyChemister();
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player2, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        preparePriority();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, bolt.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bolt);
    }

    @Test
    @DisplayName("The sacrifice cost is paid even when no cards have been exiled")
    void sacrificeAbilityWithNoExiledCards() {
        addReadyChemister();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        preparePriority();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Izzet Chemister");
        harness.assertInGraveyard(player1, "Izzet Chemister");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyChemister() {
        Permanent chemister = harness.addToBattlefieldAndReturn(player1, new IzzetChemister());
        chemister.setSummoningSick(false);
        return chemister;
    }

    private void preparePriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
