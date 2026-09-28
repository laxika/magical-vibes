package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BraveTheSands;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamuraiOfThePaleCurtain.class, SakuraTribeElder.class, RendFlesh.class, BraveTheSands.class})
class SamuraiOfThePaleCurtainTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido gives Samurai +1/+1 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SamuraiOfThePaleCurtain());
        addCreatureReady(player2, new SakuraTribeElder());

        samurai.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bushido gives Samurai +1/+1 when it blocks")
    void blocksGetsBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SamuraiOfThePaleCurtain());
        Permanent attacker = addCreatureReady(player2, new SakuraTribeElder());

        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
    }

    @Test
    @DisplayName("Permanents put into graveyards are exiled instead")
    void permanentsAreExiledInsteadOfEnteringGraveyards() {
        addCreatureReady(player1, new SamuraiOfThePaleCurtain());
        addCreatureReady(player1, new SakuraTribeElder());
        addCreatureReady(player2, new SakuraTribeElder());
        harness.setHand(player1, List.of(new RendFlesh(), new RendFlesh(), new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 9);

        destroyWithRendFlesh(player1, "Sakura-Tribe Elder");
        destroyWithRendFlesh(player2, "Sakura-Tribe Elder");
        destroyWithRendFlesh(player1, "Samurai of the Pale Curtain");

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Sakura-Tribe Elder")
                        || card.getName().equals("Samurai of the Pale Curtain"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Sakura-Tribe Elder"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sakura-Tribe Elder"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Sakura-Tribe Elder"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Samurai of the Pale Curtain"));
    }

    @Test
    @DisplayName("A nonpermanent spell put into a graveyard is not exiled")
    void nonPermanentCardsAreNotExiled() {
        addCreatureReady(player1, new SamuraiOfThePaleCurtain());
        Permanent target = addCreatureReady(player2, new SakuraTribeElder());
        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Rend Flesh");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Sakura-Tribe Elder"));
    }

    @Test
    @DisplayName("Bushido gives only one bonus when Samurai blocks multiple creatures")
    void blocksMultipleCreaturesGetsOneBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SamuraiOfThePaleCurtain());
        harness.addToBattlefield(player1, new BraveTheSands());
        Permanent firstAttacker = addCreatureReady(player2, new SakuraTribeElder());
        Permanent secondAttacker = addCreatureReady(player2, new SakuraTribeElder());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(gd.stack.stream()
                .filter(entry -> samurai.getId().equals(entry.getSourcePermanentId())))
                .hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
    }

    private void destroyWithRendFlesh(
            com.github.laxika.magicalvibes.model.Player targetOwner,
            String targetName) {
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(targetOwner, targetName));
    }
}
