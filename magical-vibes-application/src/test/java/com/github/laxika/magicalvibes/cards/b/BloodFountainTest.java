package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodFountain.class, GrizzlyBears.class, LightningBolt.class})
class BloodFountainTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Blood token")
    void entersWithBloodToken() {
        harness.setHand(player1, List.of(new BloodFountain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> permanents = gd.playerBattlefields.get(player1.getId());
        assertThat(permanents).hasSize(2);
        assertThat(permanents).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifices itself and returns up to two creature cards")
    void sacrificesAndReturnsTwoCreatureCards() {
        Permanent fountain = addFountain();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second)));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(fountain), 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fountain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fountain.getCard());
        assertThat(handIds(player1)).contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Allows returning only one creature card")
    void returnsOnlyOneCreatureCard() {
        Permanent fountain = addFountain();
        Card creature = new GrizzlyBears();
        Card otherCreature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature, otherCreature)));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(fountain), 0,
                List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(creature.getId()).doesNotContain(otherCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature);
    }

    @Test
    @DisplayName("Rejects a noncreature graveyard target")
    void rejectsNoncreatureTarget() {
        Permanent fountain = addFountain();
        Card bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(fountain), 0, List.of(bolt.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fountain);
    }

    private Permanent addFountain() {
        return harness.addToBattlefieldAndReturn(player1, new BloodFountain());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int battlefieldIndex(Permanent fountain) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(fountain);
    }

    private List<java.util.UUID> handIds(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
    }
}
