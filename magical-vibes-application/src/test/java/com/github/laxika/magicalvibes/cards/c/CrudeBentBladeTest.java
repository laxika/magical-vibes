package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrudeBentBlade.class, GiantSpider.class, GrizzlyBears.class})
class CrudeBentBladeTest extends BaseCardTest {

    @Test
    void enteringMakesTargetOpponentChooseAcreatureToSacrifice() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        harness.setHand(player1, List.of(new CrudeBentBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player2, spider.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears).doesNotContain(spider);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spider.getCard());
    }

    @Test
    void equippedCreatureGetsPlusTwoPlusOne() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new CrudeBentBlade());
        blade.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    void equipTwoAttachesToCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new CrudeBentBlade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new CrudeBentBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringSacrificesTheOpponentsOnlyCreatureAndLeavesControllersCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CrudeBentBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature.getCard());
    }

    @Test
    void enteringWithNoOpposingCreaturesLeavesNoncreaturesAndOwnCreaturesAlone() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new CrudeBentBlade());
        harness.setHand(player1, List.of(new CrudeBentBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingEquipment);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void reequippingMovesTheBonusAndDoesNotRepeatTheEnterTrigger() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new CrudeBentBlade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        blade.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, spider.getId());
        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(spider.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new CrudeBentBlade());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new CrudeBentBlade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }
    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new CrudeBentBlade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
