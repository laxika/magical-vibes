package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

class WhipOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have lifelink")
    void grantsLifelinkToControlledCreatures() {
        harness.addToBattlefield(player1, new WhipOfErebos());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opposingCreature = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Reanimates a target creature with haste")
    void reanimatesCreatureWithHaste() {
        Permanent whip = addReadyWhip();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(player1, whip), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent reanimated = findPermanent(player1, "Grizzly Bears");
        assertThat(reanimated.getCard().getId()).isEqualTo(creature.getId());
        assertThat(reanimated.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Reanimated creature is exiled at the beginning of the next end step")
    void exilesReanimatedCreatureAtNextEndStep() {
        reanimateCreature();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Reanimated creature is exiled instead of going to the graveyard")
    void exilesReanimatedCreatureIfItWouldLeaveBattlefield() {
        reanimateCreature();
        Permanent reanimated = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, reanimated.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(reanimated.getCard().getId()));
    }

    @Test
    @DisplayName("The ability cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Permanent whip = addReadyWhip();
        Card instant = new LightningBolt();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, whip), 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    private void reanimateCreature() {
        Permanent whip = addReadyWhip();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(player1, whip), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
    }

    private Permanent addReadyWhip() {
        Permanent whip = new Permanent(new WhipOfErebos());
        whip.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(whip);
        return whip;
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
