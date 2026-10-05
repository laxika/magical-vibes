package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalametScythe.class, MalametBrawler.class, Abrade.class})
class MalametScytheTest extends BaseCardTest {

    @Test
    void canBeCastDuringOpponentsEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new MalametScythe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Malamet Scythe").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void cannotEquipAtInstantSpeedDespiteFlash() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        harness.addToBattlefield(player1, new MalametScythe());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersUnattachedUntilItsTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        harness.setHand(player1, List.of(new MalametScythe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent scythe = findPermanent(player1, "Malamet Scythe");
        assertThat(scythe.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void staysUnattachedWhenTriggerTargetDiesInResponse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        harness.setHand(player1, List.of(new MalametScythe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Malamet Brawler");
        assertThat(findPermanent(player1, "Malamet Scythe").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersAttachedAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        harness.setHand(player1, List.of(new MalametScythe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent scythe = findPermanent(player1, "Malamet Scythe");
        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void destroyingScytheRemovesItsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        harness.setHand(player1, List.of(new MalametScythe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent scythe = findPermanent(player1, "Malamet Scythe");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, scythe.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Malamet Scythe");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void equipMovesScytheToAnotherCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        harness.setHand(player1, List.of(new MalametScythe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, firstCreature.getId());
        resolveAllTriggers();

        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        Permanent scythe = findPermanent(player1, "Malamet Scythe");
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int scytheIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scythe);
        harness.activateAbility(player1, scytheIndex, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
    }

    @Test
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MalametBrawler());
        harness.setHand(player1, List.of(new MalametScythe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersWithoutAttachmentWhenNoCreatureIsControlled() {
        harness.setHand(player1, List.of(new MalametScythe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent scythe = findPermanent(player1, "Malamet Scythe");
        assertThat(scythe.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
