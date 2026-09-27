package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GethThaneOfContractsTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get -1/-1")
    void debuffsOtherCreaturesYouControl() {
        addCreatureReady(player1, new GethThaneOfContracts());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        Permanent geth = findPermanent(player1, "Geth, Thane of Contracts");
        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opposingCreature = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, geth)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geth)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a target creature from your graveyard with the exile replacement")
    void returnsTargetCreatureWithExileReplacement() {
        Permanent geth = addReadyGeth();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addGethMana();

        activateGeth(geth, creature);

        Permanent reanimated = findPermanent(player1, "Grizzly Bears");
        assertThat(reanimated.getCard().getId()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(reanimated.isExileIfLeavesBattlefield()).isTrue();
    }

    @Test
    @DisplayName("Exiles the reanimated creature instead of returning it to the graveyard")
    void exilesReanimatedCreatureWhenItLeavesBattlefield() {
        Permanent geth = addReadyGeth();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addGethMana();
        activateGeth(geth, creature);

        Permanent reanimated = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, reanimated.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent geth = addReadyGeth();
        Card instant = new LightningBolt();
        harness.setGraveyard(player1, List.of(instant));
        addGethMana();

        assertThatThrownBy(() -> activateGeth(geth, instant))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    private Permanent addReadyGeth() {
        return addCreatureReady(player1, new GethThaneOfContracts());
    }

    private void addGethMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void activateGeth(Permanent geth, Card card) {
        harness.activateAbility(player1, battlefieldIndex(geth), 0, null, card.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
