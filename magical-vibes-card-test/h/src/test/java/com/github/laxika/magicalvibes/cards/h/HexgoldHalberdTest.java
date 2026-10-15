package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HexgoldHalberd.class, GrizzlyBears.class})
class HexgoldHalberdTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates and attaches a 2/2 Rebel token")
    void forMirrodinCreatesAndAttachesRebel() {
        harness.setHand(player1, List.of(new HexgoldHalberd()));
        addManaForHexgoldHalberd();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent halberd = findPermanent(player1, "Hexgold Halberd");

        assertThat(halberd.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has first strike and trample during the controller's turn")
    void equippedCreatureHasKeywordsDuringControllerTurn() {
        Permanent halberd = addHalberdReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        halberd.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature loses first strike and trample during an opponent's turn")
    void equippedCreatureLosesKeywordsDuringOpponentTurn() {
        Permanent halberd = addHalberdReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        halberd.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equip moves Hexgold Halberd and its keywords to another creature")
    void equipMovesHalberdToAnotherCreature() {
        Permanent halberd = addHalberdReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        halberd.setAttachedTo(creature1.getId());
        harness.forceActivePlayer(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("For Mirrodin! still creates a Rebel when the Equipment leaves before resolution")
    void createsRebelAfterEquipmentLeaves() {
        harness.setHand(player1, List.of(new HexgoldHalberd()));
        addManaForHexgoldHalberd();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent halberd = findPermanent(player1, "Hexgold Halberd");
        assertThat(countPermanents(player1, "Rebel")).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(halberd);
        gd.playerGraveyards.get(player1.getId()).add(halberd.getCard());
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(countPermanents(player1, "Rebel")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The Equipment controller's turn determines keywords even on an opponent's creature")
    void keywordsFollowEquipmentControllerTurn() {
        Permanent halberd = addHalberdReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        halberd.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Moving the Equipment away from the Rebel leaves the Rebel alive")
    void rebelSurvivesEquipToAnotherCreature() {
        harness.setHand(player1, List.of(new HexgoldHalberd()));
        addManaForHexgoldHalberd();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent halberd = findPermanent(player1, "Hexgold Halberd");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(findPermanent(player1, "Rebel")).isSameAs(rebel);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addHalberdReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new HexgoldHalberd());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addManaForHexgoldHalberd() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
