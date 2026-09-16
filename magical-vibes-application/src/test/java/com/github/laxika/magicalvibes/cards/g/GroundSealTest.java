package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CryptCreeper;
import com.github.laxika.magicalvibes.cards.d.DruidLyrist;
import com.github.laxika.magicalvibes.cards.m.Mudhole;
import com.github.laxika.magicalvibes.cards.p.PetrifiedField;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GroundSeal.class, Zombify.class, DruidLyrist.class, CryptCreeper.class,
        Mudhole.class, PetrifiedField.class})
class GroundSealTest extends BaseCardTest {

    @Test
    @DisplayName("Ground Seal draws a card when it enters")
    void drawsCardOnEnter() {
        Card libraryCard = new DruidLyrist();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new GroundSeal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        // Resolve the ETB trigger — draw a card
        harness.passBothPriorities();

        harness.assertInHand(player1, libraryCard.getName());
        harness.assertOnBattlefield(player1, "Ground Seal");
    }

    @Test
    @DisplayName("Ground Seal stops a spell from targeting a card in its controller's graveyard")
    void blocksTargetingOwnGraveyard() {
        Card creature = new DruidLyrist();
        harness.addToBattlefield(player1, new GroundSeal());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ground Seal is symmetric — the opponent also can't target a graveyard card")
    void blocksTargetingForOpponent() {
        Card creature = new DruidLyrist();
        harness.addToBattlefield(player1, new GroundSeal());
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player2, List.of(new Zombify()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without Ground Seal the same graveyard target is legal")
    void graveyardTargetingWorksWithoutGroundSeal() {
        Card creature = new DruidLyrist();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Druid Lyrist");
    }

    @Test
    @DisplayName("Ground Seal also stops an ability from targeting a graveyard card")
    void blocksAbilityTargetingGraveyard() {
        harness.addToBattlefield(player1, new GroundSeal());
        Permanent creeper = addCreatureReady(player1, new CryptCreeper());
        Card target = new DruidLyrist();
        harness.setGraveyard(player1, List.of(target));

        int creeperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, creeperIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creeper);
        harness.assertInGraveyard(player1, "Druid Lyrist");
    }

    @Test
    @DisplayName("Ground Seal does not stop non-targeting graveyard effects")
    void allowsNonTargetingGraveyardEffects() {
        harness.addToBattlefield(player1, new GroundSeal());
        Card land = new PetrifiedField();
        Card creature = new DruidLyrist();
        harness.setGraveyard(player2, List.of(land, creature));
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotInGraveyard(player2, "Petrified Field");
        harness.assertInGraveyard(player2, "Druid Lyrist");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
    }
}
